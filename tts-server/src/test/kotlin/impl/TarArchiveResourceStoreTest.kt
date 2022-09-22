package ccom.gitlab.sszuev.flashcards.speaker.impl

import com.gitlab.sszuev.flashcards.speaker.impl.TarArchiveResourceStore
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.security.MessageDigest

class TarArchiveResourceStoreTest {
    companion object {
        private const val archiveName = "weather.tar"
        private const val lang = "data/en"
        private const val resourceArchive = "/$lang/$archiveName"

        private const val wordName = "weather"
        private const val wordId = "$wordName.flac"
        private const val wordResourceSize: Int = 35851
        private val wordResourceMD5 =
            byteArrayOf(-76, 97, 23, 2, -103, 119, -4, -107, 59, 117, 33, -128, -25, 51, 112, 114)
    }

    @Test
    fun testGetResourceId() {
        val provider = TarArchiveResourceStore {
            TarArchiveResourceStoreTest::class.java.getResourceAsStream(resourceArchive)!!
        }
        val id = provider.getResourcePath(wordName)
        Assertions.assertEquals(wordId, id)
    }

    @Test
    fun testGetResource() {
        val provider = TarArchiveResourceStore {
            TarArchiveResourceStoreTest::class.java.getResourceAsStream(resourceArchive)!!
        }
        val res = provider.getResource(wordId)
        Assertions.assertEquals(wordResourceSize, res.size)

        val md = MessageDigest.getInstance("MD5")
        md.update(res)
        Assertions.assertArrayEquals(wordResourceMD5, md.digest())
    }
}