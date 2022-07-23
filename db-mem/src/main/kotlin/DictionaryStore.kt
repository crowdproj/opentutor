package com.gitlab.sszuev.flashcards.dbmem

import com.gitlab.sszuev.flashcards.dbmem.dao.Dictionary
import com.gitlab.sszuev.flashcards.dbmem.documents.impl.LingvoDictionaryReader
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.createDirectories
import kotlin.io.path.inputStream
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.streams.asSequence

object DictionaryStore {
    private val logger = LoggerFactory.getLogger(DictionaryStore::class.java)

    private val reader = LingvoDictionaryReader()
    private val resources = ConcurrentHashMap<String, List<Dictionary>>()

    fun getDictionaries(location: String): List<Dictionary> {
        return resources.computeIfAbsent(location) { loadDatabase(it) }
    }

    private fun loadDatabase(location: String): List<Dictionary> {
        return if (location.startsWith("classpath:")) {
            loadDatabaseFromClassPath(location.removePrefix("classpath:"))
        } else loadDatabaseFromDirectory(location)
    }

    private fun loadDatabaseFromClassPath(location: String): List<Dictionary> {
        logger.info("Load from classpath: $location.")
        val resources: List<String> = requireNotNull(DictionaryStore::class.java.getResourceAsStream(location)) {
            "Can't find classpath directory $location."
        }.bufferedReader(Charsets.UTF_8).use { br ->
            // java Stream to Sequence and then to List due to compilation error with type inferencing
            br.lines().asSequence().toList()
        }.sorted() // sort to make output deterministic
        val res: List<Dictionary> = resources.map {
            requireNotNull(DictionaryStore::class.java.getResourceAsStream("$location/$it")) {
                "Can't find classpath resource $location/$it."
            }.use { src ->
                reader.parse(src)
            }
        }
        logger.info("For location=$location there are ${res.size} dictionaries loaded.")
        return res
    }

    private fun loadDatabaseFromDirectory(location: String): List<Dictionary> {
        logger.info("Load from directory: $location.")
        val p = Paths.get(location).createDirectories().toRealPath()
        require(p.isDirectory()) { "Not a directory: $location." }
        val res: List<Dictionary> = Files.newDirectoryStream(p).use { ds ->
            ds.mapNotNull { f ->
                if (!f.isRegularFile()) {
                    logger.warn("Not a file: $f.")
                    return@mapNotNull null
                }
                f
            }
        }.sortedBy {
            // sort by name to make result deterministic
            it.fileName.toString()
        }.map { file ->
            file.inputStream().use {
                reader.parse(it)
            }
        }
        logger.info("For location=$location there are ${res.size} dictionaries loaded.")
        return res
    }

}