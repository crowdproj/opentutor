package com.gitlab.sszuev.flashcards.dbcommon

import com.gitlab.sszuev.flashcards.repositories.DbUser
import com.gitlab.sszuev.flashcards.repositories.DbUserRepository
import kotlinx.datetime.Clock
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.concurrent.atomic.AtomicInteger

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
        val res = repository.createUser(DbUser(id = "42", details = mapOf("A" to 42, "B" to true, "C" to "c")))
        Assertions.assertEquals("42", res.id)
        Assertions.assertTrue(res.createdAt.toEpochMilliseconds() >= s.toEpochMilliseconds())
        Assertions.assertTrue(res.createdAt.toEpochMilliseconds() <= Clock.System.now().toEpochMilliseconds())
        Assertions.assertEquals(mapOf("A" to 42, "B" to true, "C" to "c"), res.details)
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
        val res3 = repository.updateUser(res2)

        val res4 = repository.findByUserId("c9a414f5-3f75-4494-b664-f4c8b33ff4e6")
        Assertions.assertNotNull(res4)
        Assertions.assertEquals(res2, res4)
        Assertions.assertEquals(res2, res3)
    }

    @Test
    fun `test find or create`() {
        val res1 = repository.findOrCreateUser("c9a414f5-3f75-4494-b664-f4c8b33ff4e6")
        Assertions.assertEquals("c9a414f5-3f75-4494-b664-f4c8b33ff4e6", res1.id)

        val count = AtomicInteger(0)
        val res2 = repository.findOrCreateUser(
            id = "xxx",
            details = mapOf("XXX" to 42),
            onCreate = { count.incrementAndGet() })
        Assertions.assertEquals(1, count.get())
        Assertions.assertEquals("xxx", res2.id)
        Assertions.assertEquals(mapOf("XXX" to 42), res2.details)

        val res3 = repository.findOrCreateUser(
            id = "xxx",
            details = mapOf("QQQ" to 13),
            onCreate = { count.incrementAndGet() })
        Assertions.assertEquals(1, count.get())
        Assertions.assertEquals(res2, res3)
    }

    @Test
    fun `test create and add user details and find user`() {
        val res1 = repository.createUser(DbUser("fff", details = mapOf("A" to 42, "B" to true, "C" to "c")))
        Assertions.assertEquals("fff", res1.id)

        val res2 = repository.addUserDetails("fff", mapOf("X" to 42))
        Assertions.assertEquals("fff", res2.id)
        Assertions.assertEquals(mapOf("A" to 42, "B" to true, "C" to "c", "X" to 42), res2.details)

        val res3 = repository.findByUserId("fff")!!
        Assertions.assertEquals(res2, res3)
    }
}