package com.gitlab.sszuev.flashcards.dbmem

import com.gitlab.sszuev.flashcards.common.SysConfig
import com.gitlab.sszuev.flashcards.dbmem.dao.Dictionary
import com.gitlab.sszuev.flashcards.dbmem.dao.IdSequences
import com.gitlab.sszuev.flashcards.dbmem.documents.DictionaryWriter
import com.gitlab.sszuev.flashcards.dbmem.documents.impl.LingvoDictionaryReader
import com.gitlab.sszuev.flashcards.dbmem.documents.impl.LingvoDictionaryWriter
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.timer
import kotlin.io.path.createDirectories
import kotlin.io.path.inputStream
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.streams.asSequence

/**
 * A dictionary store, attached to file system or classpath.
 * In the first case it is persistent.
 */
class DictionaryStore private constructor(
    private val resources: MutableMap<Long, Pair<Path, Dictionary>>,
    internal val ids: IdSequences,
    dbConfig: MemDbConfig,
    sysConfig: SysConfig,
) {

    /**
     * A queue to flushing data to disk.
     */
    private val dictionariesToFlush = ConcurrentHashMap<Long, Pair<Path, Dictionary>>()

    private val writer: DictionaryWriter = LingvoDictionaryWriter(sysConfig)

    init {
        timer("flush-dictionaries-to-disk", daemon = true, period = dbConfig.dataFlushPeriodInMs) {
            dictionariesToFlush.keys.toList().mapNotNull { dictionariesToFlush.remove(it) }.forEach {
                logger.debug("Save dictionary ${it.second.id} to disk.")
                Files.newOutputStream(it.first).use { out ->
                    writer.write(it.second, out)
                }
            }
        }
    }

    val size: Long
        get() = resources.size.toLong()

    val keys: Set<Long>
        get() = resources.keys

    operator fun get(id: Long): Dictionary? {
        return resources[id]?.second
    }

    /**
     * Flushes the specified dictionary data to disk.
     * It works only if this store is attached to physical directory.
     */
    fun flush(id: Long) {
        resources[id]?.takeIf { it.first != classpathPathMarker }?.let {
            dictionariesToFlush[it.second.id] = it
        }
    }

    companion object {
        private val classpathPathMarker: Path = Path.of(UUID.randomUUID().toString())
        private val logger = LoggerFactory.getLogger(DictionaryStore::class.java)

        /**
         * Global dictionary store registry.
         */
        private val stores = ConcurrentHashMap<String, DictionaryStore>()

        /**
         * Back door for testing
         */
        internal fun clear() {
            stores.clear()
        }

        /**
         * Loads dictionary store from directory.
         * @param [location][Path] - path to directory
         * @param [ids][IdSequences]
         * @param [dbConfig][MemDbConfig]
         * @param [sysConfig][SysConfig]
         */
        fun load(
            location: Path,
            ids: IdSequences = IdSequences.globalIdsGenerator,
            dbConfig: MemDbConfig = MemDbConfig(),
            sysConfig: SysConfig = SysConfig(),
        ): DictionaryStore {
            return stores.computeIfAbsent(location.toString()) {
                DictionaryStore(loadDatabaseFromDirectory(it, ids), ids, dbConfig, sysConfig)
            }
        }

        /**
         * Loads dictionary store from classpath or directory.
         * @param [location][String] - either dir path or classpath to dir with data
         * @param [ids][IdSequences]
         * @param [dbConfig][MemDbConfig]
         * @param [sysConfig][SysConfig]
         */
        fun load(
            location: String,
            ids: IdSequences = IdSequences.globalIdsGenerator,
            dbConfig: MemDbConfig = MemDbConfig(),
            sysConfig: SysConfig = SysConfig(),
        ): DictionaryStore {
            return stores.computeIfAbsent(location) { DictionaryStore(loadDatabase(it, ids), ids, dbConfig, sysConfig) }
        }

        private fun loadDatabase(location: String, ids: IdSequences): MutableMap<Long, Pair<Path, Dictionary>> {
            return if (location.startsWith("classpath:")) {
                loadDatabaseFromClassPath(location.removePrefix("classpath:"), ids)
            } else loadDatabaseFromDirectory(location, ids)
        }

        private fun loadDatabaseFromClassPath(
            classpathLocation: String,
            ids: IdSequences
        ): MutableMap<Long, Pair<Path, Dictionary>> {
            logger.info("Load dictionaries from classpath: $classpathLocation.")
            val reader = LingvoDictionaryReader(ids)
            val files: List<String> =
                requireNotNull(DictionaryStore::class.java.getResourceAsStream(classpathLocation)) {
                    "Can't find classpath directory $classpathLocation."
                }.bufferedReader(Charsets.UTF_8).use { br ->
                    // java Stream to Sequence and then to List due to compilation error with type inferencing
                    br.lines().asSequence().toList()
                }.filter {
                    return@filter if (!it.endsWith(".xml")) {
                        logger.debug("Not a xml file: $it.")
                        false
                    } else {
                        true
                    }
                }.sorted() // sort to make output deterministic
            val res: List<Pair<Path, Dictionary>> = files.map {
                requireNotNull(DictionaryStore::class.java.getResourceAsStream("$classpathLocation/$it")) {
                    "Can't find classpath resource $classpathLocation/$it."
                }.use { src ->
                    classpathPathMarker to reader.parse(src)
                }
            }
            logger.info("For location=$classpathLocation there are ${res.size} dictionaries loaded.")
            return res.associateByTo(ConcurrentHashMap<Long, Pair<Path, Dictionary>>()) { it.second.id }
        }

        private fun loadDatabaseFromDirectory(
            directoryLocation: String,
            ids: IdSequences
        ): MutableMap<Long, Pair<Path, Dictionary>> {
            logger.info("Load dictionaries from directory: $directoryLocation.")
            val reader = LingvoDictionaryReader(ids)
            val dir = Paths.get(directoryLocation).createDirectories().toRealPath()
            require(dir.isDirectory()) { "Not a directory: $directoryLocation." }
            val res: List<Pair<Path, Dictionary>> = Files.newDirectoryStream(dir).use { ds ->
                ds.mapNotNull { file ->
                    if (!file.isRegularFile()) {
                        logger.warn("Not a file: $file.")
                        return@mapNotNull null
                    }
                    if (!file.fileName.toString().endsWith(".xml")) {
                        logger.debug("Not a xml file: $file.")
                        return@mapNotNull null
                    }
                    file
                }
            }.sortedBy {
                // sort by name to make result deterministic
                it.fileName.toString()
            }.map { file ->
                file.inputStream().use {
                    file to reader.parse(it)
                }
            }
            logger.info("For location=$directoryLocation there are ${res.size} dictionaries loaded.")
            return res.associateByTo(ConcurrentHashMap<Long, Pair<Path, Dictionary>>()) { it.second.id }
        }
    }
}