package com.gitlab.sszuev.flashcards.dbmem

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.createFile
import kotlin.io.path.outputStream

internal class DictionaryStoreTest {
    @Test
    fun `test load from class-path`() {
        val dictionaries = DictionaryStore.getDictionaries("classpath:/data")
        Assertions.assertEquals(2, dictionaries.size)
        Assertions.assertEquals("Business vocabulary (Job)", dictionaries[0].name)
        Assertions.assertEquals("Weather", dictionaries[1].name)
    }

    @Test
    fun `test load from directory`(@TempDir dir: Path) {
        DictionaryStoreTest::class.java.getResourceAsStream("/data")!!
            .bufferedReader().use { bf ->
                bf.lines().forEach { r ->
                    val file = dir.resolve(r).createFile()
                    DictionaryStoreTest::class.java.getResourceAsStream("/data/$r")!!.use { src ->
                        file.outputStream().use { dst ->
                            src.copyTo(dst)
                        }
                    }
                }
            }
        val dictionaries = DictionaryStore.getDictionaries(dir.toString())
        Assertions.assertEquals(2, dictionaries.size)
        Assertions.assertEquals("Business vocabulary (Job)", dictionaries[0].name)
        Assertions.assertEquals("Weather", dictionaries[1].name)
    }
}
