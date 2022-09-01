package com.gitlab.sszuev.flashcards.dbcommon.mocks

import com.gitlab.sszuev.flashcards.model.domain.UserUid
import com.gitlab.sszuev.flashcards.repositories.DbUserRepository
import com.gitlab.sszuev.flashcards.repositories.UserEntityDbResponse

class MockDbUserRepository(
    private val invokeGetUser: (UserUid) -> UserEntityDbResponse = { UserEntityDbResponse.EMPTY }
) : DbUserRepository {

    override fun getUser(uid: UserUid): UserEntityDbResponse {
        return invokeGetUser(uid)
    }
}