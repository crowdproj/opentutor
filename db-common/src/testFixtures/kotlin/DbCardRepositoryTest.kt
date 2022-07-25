package com.gitlab.sszuev.flashcards.dbcommon

import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
import com.gitlab.sszuev.flashcards.repositories.DbCardRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

/**
 * Note: all implementations must have the same ids in tests for the same entities to have deterministic behavior.
 */
@Suppress("FunctionName")
abstract class DbCardRepositoryTest {

    abstract val repository: DbCardRepository

    @Test
    fun `test get all cards success`() {
        // Business dictionary
        val res1 = repository.getAllCards(DictionaryId("1"))
        Assertions.assertEquals(242, res1.cards.size)
        Assertions.assertEquals(0, res1.errors.size)

        // Weather dictionary
        val res2 = repository.getAllCards(DictionaryId("2"))
        Assertions.assertEquals(65, res2.cards.size)
        Assertions.assertEquals(0, res2.errors.size)
    }

    @Test
    fun `test get all cards error`() {
        val res = repository.getAllCards(DictionaryId("3"))
        Assertions.assertEquals(0, res.cards.size)
        Assertions.assertEquals(1, res.errors.size)
        val error = res.errors[0]
        Assertions.assertEquals("database::getAllCards", error.code)
        Assertions.assertEquals("3", error.field)
        Assertions.assertEquals("database", error.group)
        Assertions.assertEquals("""Error while getAllCards: dictionary with id="3" not found""", error.message)
        Assertions.assertNull(error.exception)
    }
}