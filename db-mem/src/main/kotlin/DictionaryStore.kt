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

    val size: Long
        get() = resources.size.toLong()

    /**
     * A queue to flushing data to disk.
     */
    private val dictionariesToFlush = ConcurrentHashMap<Long, Pair<Path, Dictionary>>()

    private val writer: DictionaryWriter = LingvoDictionaryWriter(sysConfig)

    init {
        timer("flush-data-to-disk", daemon = true, period = dbConfig.dataFlushPeriodInMs) {
            dictionariesToFlush.keys.toList().mapNotNull { dictionariesToFlush.remove(it) }.forEach {
                logger.debug("Save dictionary ${it.second.id} to disk.")
                Files.newOutputStream(it.first).use { out ->
                    writer.write(it.second, out)
                }
            }
        }
    }

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
         * Global id registry.
         */
        private val globalIdsGenerator: IdSequences = IdSequences()

        /**
         * Global dictionary store registry.
         */
        private val stores = ConcurrentHashMap<String, DictionaryStore>()

        fun load(
            location: Path,
            ids: IdSequences = globalIdsGenerator,
            dbConfig: MemDbConfig = MemDbConfig(),
            sysConfig: SysConfig = SysConfig(),
        ): DictionaryStore {
            return stores.computeIfAbsent(location.toString()) {
                DictionaryStore(loadDatabaseFromDirectory(it, ids), ids, dbConfig, sysConfig)
            }
        }

        fun load(
            location: String,
            ids: IdSequences = globalIdsGenerator,
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
            logger.info("Load from classpath: $classpathLocation.")
            val reader = LingvoDictionaryReader(ids)
            val dir: List<String> = requireNotNull(DictionaryStore::class.java.getResourceAsStream(classpathLocation)) {
                "Can't find classpath directory $classpathLocation."
            }.bufferedReader(Charsets.UTF_8).use { br ->
                // java Stream to Sequence and then to List due to compilation error with type inferencing
                br.lines().asSequence().toList()
            }.sorted() // sort to make output deterministic
            val res: List<Pair<Path, Dictionary>> = dir.map {
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
            logger.info("Load from directory: $directoryLocation.")
            val reader = LingvoDictionaryReader(ids)
            val dir = Paths.get(directoryLocation).createDirectories().toRealPath()
            require(dir.isDirectory()) { "Not a directory: $directoryLocation." }
            val res: List<Pair<Path, Dictionary>> = Files.newDirectoryStream(dir).use { ds ->
                ds.mapNotNull { file ->
                    if (!file.isRegularFile()) {
                        logger.warn("Not a file: $file.")
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