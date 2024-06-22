package com.gitlab.sszuev.flashcards.dbmem

import com.gitlab.sszuev.flashcards.dbmem.dao.MemDbCard
import com.gitlab.sszuev.flashcards.dbmem.dao.MemDbUser
import com.gitlab.sszuev.flashcards.dbmem.dao.MemDbWord
import com.gitlab.sszuev.flashcards.dbmem.testutils.classPathResourceDir
import com.gitlab.sszuev.flashcards.dbmem.testutils.copyClassPathDataToDir
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import java.time.LocalDateTime
import java.util.UUID

@Order(1)
internal class MemDatabaseTest {

    companion object {
        private val existingUUID: UUID = UUID.fromString("c9a414f5-3f75-4494-b664-f4c8b33ff4e6")

        private val testCard = MemDbCard(
            id = null,
            words = listOf(MemDbWord(word = "word", translations = listOf(listOf("слово")))),
        )
    }

    @Test
    fun `test find user`() {
        val database = MemDatabase.load("classpath:$classPathResourceDir")
        Assertions.assertEquals(existingUUID.toString(), database.findUserById(existingUUID.toString())?.id)
    }

    @Test
    fun `test find users`() {
        val database = MemDatabase.load("classpath:$classPathResourceDir")
        val users = database.findUserIds().toList()
        Assertions.assertEquals(listOf(existingUUID.toString()), users)
    }

    @Test
    fun `test save user & count`(@TempDir dir: Path) {
        copyClassPathDataToDir(classPathResourceDir, dir)
        val database1 = MemDatabase.get(dir.toString())
        Assertions.assertEquals(1L, database1.countUsers())
        Assertions.assertEquals("42", database1.saveUser(MemDbUser("42", LocalDateTime.now())).id)
        Assertions.assertEquals(2L, database1.countUsers())
        MemDatabase.clear()
        val database2 = MemDatabase.get(dir.toString())
        Assertions.assertEquals(2L, database2.countUsers())
    }

    @Test
    fun `test load dictionaries from class-path & find-dictionary-by-id & find-cards-by-dictionary-id & count-dictionary`() {
        val database = MemDatabase.load("classpath:$classPathResourceDir")
        Assertions.assertEquals(2, database.countDictionaries())
        val dictionary1 = database.findDictionaryById(1)!!
        val dictionary2 = database.findDictionaryById(2)!!
        Assertions.assertEquals("Irregular Verbs", dictionary1.name)
        Assertions.assertEquals("Weather", dictionary2.name)

        Assertions.assertEquals(244, database.findCardsByDictionaryId(1).count())
        Assertions.assertEquals(65, database.findCardsByDictionaryId(2).count())
    }

    @Test
    fun `test load from directory & reload & find-dictionaries-by-ids & count-cards & saveCard & find-card-by-id`(@TempDir dir: Path) {
        copyClassPathDataToDir(classPathResourceDir, dir)
        val database1 = MemDatabase.get(dir.toString())
        val dictionaries1 = database1.findDictionariesByIds(listOf(1, 1, 2)).toList()
        Assertions.assertEquals(listOf("Irregular Verbs", "Weather"), dictionaries1.map { it.name })
        val newCard = database1.saveCard(testCard.copy(dictionaryId = 2)) // Weather
        Assertions.assertEquals(2, newCard.dictionaryId)
        Assertions.assertEquals(310, newCard.id)

        // test cleaned: wait 0.5 second (test period is 200 ms) and reload store
        Thread.sleep(500)
        MemDatabase.clear()

        val database2 = MemDatabase.get(dir.toString())
        Assertions.assertNotSame(database1, database2)
        val dictionaries2 = database1.findDictionariesByIds(listOf(1, 2)).toList()
        Assertions.assertEquals(dictionaries1, dictionaries2)
        Assertions.assertEquals(310, database2.countCards())
        val lastNewCard2 = database2.findCardById(310)!!
        Assertions.assertEquals(newCard, lastNewCard2)
        Assertions.assertTrue(database2.deleteDictionaryById(1)) // delete Irregular Verbs

        // test cleaned: wait 0.5 second (test period is 200 ms) and reload store
        Thread.sleep(500)
        MemDatabase.clear()

        val database3 = MemDatabase.get(dir.toString())
        Assertions.assertNotSame(database1, database3)
        val dictionaries3 = database3.findDictionariesByIds(listOf(1, 2)).toList()
        Assertions.assertEquals(1, dictionaries3.size)
        Assertions.assertEquals(dictionaries1[1], dictionaries3[0]) // Weather
        val lastNewCard3 = database3.findCardById(310)!!
        Assertions.assertNotSame(newCard, lastNewCard3)
        Assertions.assertEquals(newCard, lastNewCard3)

        MemDatabase.clear()
    }

}
