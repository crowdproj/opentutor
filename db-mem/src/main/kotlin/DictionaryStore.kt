package com.gitlab.sszuev.flashcards.dbmem

import com.gitlab.sszuev.flashcards.common.SysConfig
import com.gitlab.sszuev.flashcards.common.documents.createReader
import com.gitlab.sszuev.flashcards.common.documents.createWriter
import com.gitlab.sszuev.flashcards.common.status
import com.gitlab.sszuev.flashcards.dbmem.dao.MemDbDictionary
import org.slf4j.LoggerFactory
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.Paths
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
    private val resources: MutableMap<Long, Pair<String, MemDbDictionary>>,
    dbConfig: MemDbConfig,
    sysConfig: SysConfig,
) {

    /**
     * A queue to flushing data to disk.
     */
    private val dictionariesToFlush = ConcurrentHashMap<Long, Pair<String, MemDbDictionary>>()

    private val isFromFileSystem = !dbConfig.dataLocation.startsWith(classpathPrefix)

    init {
        if (isFromFileSystem) {
            timer("flush-dictionaries-to-disk", daemon = true, period = dbConfig.dataFlushPeriodInMs) {
                dictionariesToFlush.keys.toList().mapNotNull { dictionariesToFlush.remove(it) }.forEach {
                    logger.debug("Save dictionary ${it.second.id} to disk.")
                    Files.newOutputStream(Paths.get(it.first)).use { out ->
                        write(it.second, out, sysConfig)
                    }
                }
            }
        }
    }

    val size: Long
        get() = resources.size.toLong()

    val keys: Set<Long>
        get() = resources.keys

    operator fun get(id: Long): MemDbDictionary? {
        return resources[id]?.second
    }

    operator fun set(id: Long, record: MemDbDictionary) {
        val location = if (isFromFileSystem) {
            record.file()
        } else {
            classpathPrefix + record.file()
        }
        val value = location to record
        resources[id] = value
    }

    operator fun minus(id: Long): MemDbDictionary? {
        return resources.remove(id)?.second
    }

    /**
     * Flushes the specified dictionary data to disk.
     * It works only if this store is attached to physical directory.
     */
    fun flush(id: Long) {
        if (isFromFileSystem) {
            resources[id]?.let {
                dictionariesToFlush[it.second.id] = it
            }
        }
    }

    companion object {
        private const val classpathPrefix = "classpath:"
        private val logger = LoggerFactory.getLogger(DictionaryStore::class.java)

        /**
         * Global dictionary store registry.
         */
        private val stores = ConcurrentHashMap<Configuration, DictionaryStore>()

        /**
         * Back door for testing
         */
        fun clear() {
            stores.clear()
        }

        /**
         * Loads dictionary store from classpath or directory.
         * @param [ids][IdSequences]
         * @param [dbConfig][MemDbConfig]
         * @param [sysConfig][SysConfig]
         */
        fun load(
            ids: IdSequences = IdSequences.globalIdsGenerator,
            dbConfig: MemDbConfig = MemDbConfig(),
            sysConfig: SysConfig = SysConfig(),
        ): DictionaryStore {
            val location = dbConfig.dataLocation
            val fromClassPath = dbConfig.dataLocation.startsWith(classpathPrefix)
            val key = Configuration(dbConfig, sysConfig)
            return stores.computeIfAbsent(key) {
                val resources = if (fromClassPath) {
                    loadDatabaseFromClassPath(location.removePrefix(classpathPrefix), ids)
                } else loadDatabaseFromDirectory(location, ids)
                DictionaryStore(
                    resources = resources,
                    dbConfig = dbConfig,
                    sysConfig = sysConfig,
                )
            }
        }

        private fun loadDatabaseFromClassPath(
            classpathLocation: String,
            ids: IdSequences
        ): MutableMap<Long, Pair<String, MemDbDictionary>> {
            logger.info("Load dictionaries from classpath: $classpathLocation.")
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
            val res: List<Pair<String, MemDbDictionary>> = files.map {
                requireNotNull(DictionaryStore::class.java.getResourceAsStream("$classpathLocation/$it")) {
                    "Can't find classpath resource $classpathLocation/$it."
                }.use { src ->
                    "${classpathPrefix}${classpathLocation}/$it" to read(src, ids)
                }
            }
            logger.info("For location=$classpathLocation there are ${res.size} dictionaries loaded.")
            return res.associateByTo(ConcurrentHashMap<Long, Pair<String, MemDbDictionary>>()) { it.second.id }
        }

        private fun loadDatabaseFromDirectory(
            directoryLocation: String,
            ids: IdSequences
        ): MutableMap<Long, Pair<String, MemDbDictionary>> {
            logger.info("Load dictionaries from directory: $directoryLocation.")
            val dir = Paths.get(directoryLocation).createDirectories().toRealPath()
            require(dir.isDirectory()) { "Not a directory: $directoryLocation." }
            val res: List<Pair<String, MemDbDictionary>> = Files.newDirectoryStream(dir).use { ds ->
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
                    file.toString() to read(it, ids)
                }
            }
            logger.info("For location=$directoryLocation there are ${res.size} dictionaries loaded.")
            return res.associateByTo(ConcurrentHashMap<Long, Pair<String, MemDbDictionary>>()) { it.second.id }
        }

        private fun MemDbDictionary.file(): String {
            return "dictionary-${this.id}.xml"
        }

        private fun read(inputStream: InputStream, generator: IdSequences): MemDbDictionary {
            val res = createReader(generator).parse(inputStream).toDbRecord()
            generator.position(res)
            return res
        }

        private fun write(dictionary: MemDbDictionary, outputStream: OutputStream, sysConfig: SysConfig): Unit =
            createWriter().write(dictionary.toDocument { sysConfig.status(it) }, outputStream)

        data class Configuration(
            val dbConfig: MemDbConfig,
            val sysConfig: SysConfig
        )
    }
}