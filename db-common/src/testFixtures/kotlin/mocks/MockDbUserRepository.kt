package com.gitlab.sszuev.flashcards.dbcommon.mocks

import com.gitlab.sszuev.flashcards.model.common.AppAuthId
import com.gitlab.sszuev.flashcards.repositories.DbUserRepository
import com.gitlab.sszuev.flashcards.repositories.UserEntityDbResponse

class MockDbUserRepository(
    private val invokeGetUser: (AppAuthId) -> UserEntityDbResponse = { UserEntityDbResponse.EMPTY }
) : DbUserRepository {

    override fun getUser(authId: AppAuthId): UserEntityDbResponse {
        return invokeGetUser(authId)
    }
}