package com.gitlab.sszuev.flashcards.dbmem

import com.gitlab.sszuev.flashcards.dbmem.dao.IdSequences
import com.gitlab.sszuev.flashcards.dbmem.dao.User
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import java.util.*

internal class UserStoreTest {

    companion object {
        val existingUUID: UUID = UUID.fromString("c9a414f5-3f75-4494-b664-f4c8b33ff4e6")
        val newUUID: UUID = UUID.fromString("45a34bd8-5472-491e-8e27-84290314ee38")
        val existingUser = User(
            id = 42,
            uuid = existingUUID,
            role = 2,
        )
    }

    @Test
    fun `test load users from class-path`() {
        val users = UserStore.load(location = "classpath:/data", ids = IdSequences())
        Assertions.assertEquals(1, users.size)
        val user = users[existingUUID]
        Assertions.assertNotNull(user)
        Assertions.assertNotSame(existingUser, user)
        Assertions.assertEquals(existingUser, user)
    }

    @Test
    fun `test load users from directory & flush & reload`(@TempDir dir: Path) {
        val newUser = User(
            id = -42,
            uuid = newUUID,
            role = 42,
        )
        copyClassPathDataToDir(dir)
        val store1 = UserStore.load(location = dir, ids = IdSequences())
        Assertions.assertEquals(1, store1.size)
        store1 + newUser
        Assertions.assertEquals(2, store1.size)
        store1.flush()

        val store2 = UserStore.load(location = dir, ids = IdSequences())
        Assertions.assertSame(store1, store2)
        Assertions.assertEquals(2, store2.size)
        Assertions.assertEquals(existingUser, store2[existingUUID]!!)
        Assertions.assertEquals(newUser, store2[newUUID]!!)
        Assertions.assertSame(newUser, store2[newUUID]!!)

        // clear to avoid caching
        UserStore.clear()

        // wait 1 second (default period is 500 ms) and reload store
        Thread.sleep(1000)

        val store3 = UserStore.load(location = dir, ids = IdSequences())
        Assertions.assertNotSame(store1, store3)
        Assertions.assertEquals(2, store3.size)
        Assertions.assertEquals(existingUser, store3[existingUUID]!!)
        Assertions.assertEquals(newUser, store3[newUUID]!!)
        Assertions.assertNotSame(newUser, store3[newUUID]!!)
    }

}