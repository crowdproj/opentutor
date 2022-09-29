package com.gitlab.sszuev.flashcards.speaker.impl

import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.utils.IOUtils
import java.io.InputStream
import java.nio.charset.StandardCharsets

/**
 * Implementation of [ResourceStore] to work with tar archives that contain flac audio files.
 * Flac audio files contain SWAC fields encapsulated in Vorbis Comment tags.
 * @see <a href="http://shtooka.net/swac">shtooka swac</a>
 */
class TarArchiveResourceStore(source: () -> InputStream) : ResourceStore {
    private val source: () -> TarArchiveInputStream

    init {
        this.source = fromSource(source)
    }

    private val indexes: Map<String, List<Info>> by lazy {
        readIndexMap()
    }

    private fun readIndexMap(): Map<String, List<Info>> {
        val map = readEntry(INDEXES).toString(StandardCharsets.UTF_8)
        return map.split("\r*\n\r*\n".toRegex())
            .filter { it.contains(TEXT_REF) }
            .map {
                val index = parseTextIndex(it)
                index to Info(index, parseFileIndex(it))
            }.groupBy({ it.first }, { it.second })
    }

    private fun readEntry(name: String): ByteArray {
        try {
            source().use { tar ->
                var entry: TarArchiveEntry
                while (tar.nextTarEntry.also { entry = it } != null) {
                    if (name != entry.name) {
                        continue
                    }
                    val size = entry.realSize
                    check(size < Int.MAX_VALUE)
                    val res = ByteArray(size.toInt())
                    check(IOUtils.readFully(tar, res) >= 0)
                    return res
                }
                throw IllegalArgumentException("Can't find entry '$name'")
            }
        } catch (e: Exception) {
            throw IllegalStateException("Unexpected error while [$source]", e)
        }
    }

    override fun getResourcePath(word: String, vararg options: String): String? {
        val res = indexes[word]
        return if (res.isNullOrEmpty()) null else res[0].file
    }

    override fun getResource(path: String): ByteArray {
        return readEntry(DIR + path)
    }

    private data class Info(val text: String, val file: String)

    companion object {
        private val encoding = StandardCharsets.UTF_8.name()
        private const val DIR = "flac/"
        private const val TEXT_REF = "SWAC_TEXT"
        private const val INDEXES = DIR + "index.tags.txt"

        private fun fromSource(source: () -> InputStream): () -> TarArchiveInputStream {
            return {
                val res = source()
                if (res is TarArchiveInputStream) {
                    res
                } else {
                    TarArchiveInputStream(res, encoding)
                }
            }
        }

        private fun parseTextIndex(block: String): String {
            val res = block.split("\r*\n".toRegex()).filterNot { it.isBlank() }
                .mapNotNull {
                    val arr = it.split("=")
                    if (arr.size == 2 && TEXT_REF == arr[0]) arr[1] else null
                }
            if (res.size == 1) {
                return res[0]
            }
            throw IllegalStateException("Can't find $TEXT_REF in <$block>.")
        }

        private fun parseFileIndex(block: String): String {
            val res = block.split("\r*\n".toRegex()).filterNot { it.isBlank() }
                .mapNotNull {
                    if (it.startsWith("[") && it.endsWith("]")) {
                        it.replace("^\\[(.+)]$".toRegex(), "$1")
                    } else null
                }
            if (res.size == 1) {
                return res[0]
            }
            throw IllegalStateException("Can't find file in <$block>.")
        }
    }
}
