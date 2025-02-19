package com.gitlab.sszuev.flashcards.core

import com.gitlab.sszuev.flashcards.core.processes.createOrUpdateUser
import com.gitlab.sszuev.flashcards.core.processes.users
import com.gitlab.sszuev.flashcards.dbcommon.mocks.MockDbUserRepository
import com.gitlab.sszuev.flashcards.model.common.AppAuthId
import com.gitlab.sszuev.flashcards.repositories.DbUser
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.concurrent.atomic.AtomicInteger

internal class FirstLoginHelperTest {

    @Test
    fun `test create or update user`() {
        val id = AppAuthId("42")
        val data = mutableMapOf<String, DbUser>()
        val userRepository = MockDbUserRepository(
            invokeCreateUser = {
                data[it.id] = it
                it
            },
            invokeUpdateUser = {
                val new = data[it.id]!!
                data[it.id] = new
                new
            },
            invokeFindUserById = {
                data[it]
            }
        )

        val counts = AtomicInteger()
        userRepository.createOrUpdateUser(id = id, locale = "xx") {
            counts.incrementAndGet()
        }
        userRepository.createOrUpdateUser(id = id, locale = "xx") {
            counts.incrementAndGet()
        }
        Assertions.assertEquals(1, counts.get())
        Assertions.assertEquals(1, data.size)

        users.invalidateAll()
        userRepository.createOrUpdateUser(id = id, locale = "xx") {
            counts.incrementAndGet()
        }
        Assertions.assertEquals(1, counts.get())
        Assertions.assertEquals(1, data.size)
    }
}