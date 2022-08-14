package com.gitlab.sszuev.flashcards.dbmem

import com.gitlab.sszuev.flashcards.dbmem.dao.Card
import com.gitlab.sszuev.flashcards.dbmem.dao.IdSequences
import com.gitlab.sszuev.flashcards.dbmem.dao.Translation
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.createFile
import kotlin.io.path.outputStream

internal class DictionaryStoreTest {

    companion object {
        private fun saveDictionariesToDir(dir: Path) {
            DictionaryStoreTest::class.java.getResourceAsStream("/data")!!.bufferedReader().use { bf ->
                bf.lines().forEach { r ->
                    val file = dir.resolve(r).createFile()
                    DictionaryStoreTest::class.java.getResourceAsStream("/data/$r")!!.use { src ->
                        file.outputStream().use { dst ->
                            src.copyTo(dst)
                        }
                    }
                }
            }
        }
    }

    @Test
    fun `test load from class-path`() {
        val dictionaries = DictionaryStore.load(location = "classpath:/data", ids = IdSequences())
        Assertions.assertEquals(2, dictionaries.size)
        Assertions.assertEquals("Business vocabulary (Job)", dictionaries[1]!!.name)
        Assertions.assertEquals("Weather", dictionaries[2]!!.name)

        Assertions.assertEquals(242, dictionaries[1]!!.cards.size)
        Assertions.assertEquals(65, dictionaries[2]!!.cards.size)
    }

    @Test
    fun `test load from directory & flush & reload`(@TempDir dir: Path) {
        saveDictionariesToDir(dir)
        val dictionaries1 = DictionaryStore.load(location = dir, ids = IdSequences())
        Assertions.assertEquals(2, dictionaries1.size)
        Assertions.assertEquals("Business vocabulary (Job)", dictionaries1[1]!!.name)
        Assertions.assertEquals("Weather", dictionaries1[2]!!.name)

        Assertions.assertEquals(242, dictionaries1[1]!!.cards.size)
        Assertions.assertEquals(65, dictionaries1[2]!!.cards.size)

        val card = Card(
            id = -42,
            dictionaryId = 2,
            text = "word",
            translations = listOf(Translation(id = -42, text = "слово", cardId = -42))
        )
        dictionaries1[2]!!.cards[-42] = card
        // wait 1 second (default period is 500 ms) and reload store
        Thread.sleep(1000)

        val dictionaries2 = DictionaryStore.load(location = dir, ids = IdSequences())
        Assertions.assertEquals(2, dictionaries2.size)
        Assertions.assertEquals(242, dictionaries2[1]!!.cards.size)
        Assertions.assertEquals(66, dictionaries2[2]!!.cards.size)

        val actual = dictionaries2[2]!!.cards[-42]
        Assertions.assertNotNull(actual)
        Assertions.assertEquals(card, actual!!)
    }

}
