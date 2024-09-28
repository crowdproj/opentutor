package com.gitlab.sszuev.flashcards.dbcommon

import com.gitlab.sszuev.flashcards.repositories.DbUser
import com.gitlab.sszuev.flashcards.repositories.DbUserRepository
import kotlinx.datetime.Clock
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

abstract class DbUserRepositoryTest {
    abstract val repository: DbUserRepository

    @Test
    fun `test find and create`() {
        Assertions.assertEquals(
            "c9a414f5-3f75-4494-b664-f4c8b33ff4e6",
            repository.findByUserId("c9a414f5-3f75-4494-b664-f4c8b33ff4e6")?.id
        )
        Assertions.assertNull(repository.findByUserId("42"))
        val s = Clock.System.now()
        val res = repository.createUser(DbUser(id = "42", details = mapOf("A" to 42L, "B" to true, "C" to "c")))
        Assertions.assertEquals("42", res.id)
        Assertions.assertTrue(res.createdAt.toEpochMilliseconds() >= s.toEpochMilliseconds())
        Assertions.assertTrue(res.createdAt.toEpochMilliseconds() <= Clock.System.now().toEpochMilliseconds())
        Assertions.assertEquals(
            "42",
            repository.findByUserId("42")?.id
        )
    }

    @Test
    fun `test find and update`() {
        val res1 = repository.findByUserId("c9a414f5-3f75-4494-b664-f4c8b33ff4e6")
        Assertions.assertNotNull(res1)

        val res2 = res1!!.copy(details = mapOf("A" to 42, "B" to true, "C" to "c"))
        repository.updateUser(res2)

        val res3 = repository.findByUserId("c9a414f5-3f75-4494-b664-f4c8b33ff4e6")
        Assertions.assertNotNull(res3)
        println("" + res3!!.details.hashCode() + " :: " + res2.details.hashCode())
        Assertions.assertEquals(res2, res3)
    }
}