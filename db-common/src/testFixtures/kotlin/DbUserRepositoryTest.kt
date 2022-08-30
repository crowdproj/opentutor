package com.gitlab.sszuev.flashcards.dbcommon

import com.gitlab.sszuev.flashcards.model.common.AppError
import com.gitlab.sszuev.flashcards.model.domain.UserEntity
import com.gitlab.sszuev.flashcards.model.domain.UserId
import com.gitlab.sszuev.flashcards.model.domain.UserUid
import com.gitlab.sszuev.flashcards.repositories.DbUserRepository
import com.gitlab.sszuev.flashcards.repositories.UserEntityDbResponse
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

abstract class DbUserRepositoryTest {

    abstract val repository: DbUserRepository

    companion object {
        val demo = UserEntity(
            id = UserId(42.toString()),
            uid = UserUid("c9a414f5-3f75-4494-b664-f4c8b33ff4e6"),
        )

        @Suppress("SameParameterValue")
        private fun assertAppError(res: UserEntityDbResponse, uuid: String, op: String): AppError {
            Assertions.assertEquals(1, res.errors.size)
            val error = res.errors[0]
            Assertions.assertEquals("database::$op", error.code)
            Assertions.assertEquals(uuid, error.field)
            Assertions.assertEquals("database", error.group)
            return error
        }
    }

    @Test
    fun `test get user error no found`() {
        val uuid = "45a34bd8-5472-491e-8e27-84290314ee38"
        val res = repository.getUser(UserUid(uuid))
        Assertions.assertEquals(UserEntity.EMPTY, res.user)

        val error = assertAppError(res, uuid, "getUser")
        Assertions.assertEquals(
            """Error while getUser: user with uid="$uuid" not found""",
            error.message
        )
        Assertions.assertNull(error.exception)
    }

    @Test
    fun `test get user error wrong uuid`() {
        val uuid = "xxx"
        val res = repository.getUser(UserUid(uuid))
        Assertions.assertEquals(UserEntity.EMPTY, res.user)

        val error = assertAppError(res, uuid, "getUser")
        Assertions.assertEquals(
            """Error while getUser: wrong uuid="$uuid"""",
            error.message
        )
        Assertions.assertNull(error.exception)
    }

    @Test
    fun `test get user success`() {
        val res = repository.getUser(demo.uid)
        Assertions.assertNotSame(demo, res.user)
        Assertions.assertEquals(demo, res.user)
        Assertions.assertEquals(0, res.errors.size)
    }
}