package com.gitlab.sszuev.flashcards.dictionaries

import com.gitlab.sszuev.flashcards.DbRepositories
import com.gitlab.sszuev.flashcards.DictionaryContext
import com.gitlab.sszuev.flashcards.model.common.AppAuthId
import com.gitlab.sszuev.flashcards.model.domain.DictionaryEntity
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
import com.gitlab.sszuev.flashcards.model.domain.DictionaryOperation
import com.gitlab.sszuev.flashcards.model.domain.LangEntity
import com.gitlab.sszuev.flashcards.model.domain.LangId
import com.gitlab.sszuev.flashcards.repositories.DbDictionary
import com.gitlab.sszuev.flashcards.repositories.DbDictionaryRepository
import com.gitlab.sszuev.flashcards.repositories.DbLang
import com.gitlab.sszuev.flashcards.utils.dictionaryContextFromByteArray
import com.gitlab.sszuev.flashcards.utils.toByteArray
import io.mockk.every
import io.mockk.mockk
import io.nats.client.Connection
import io.nats.client.Nats
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assumptions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.time.Duration
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

@Timeout(value = 60, unit = TimeUnit.SECONDS)
@Testcontainers
class DictionariesServerProcessorTest {
    companion object {
        @Container
        private val natsContainer: GenericContainer<*> = GenericContainer(DockerImageName.parse("nats:latest"))
            .withExposedPorts(4222)

        @JvmStatic
        @AfterAll
        fun shutdown() {
            natsContainer.stop()
        }
    }

    private lateinit var connection: Connection
    private lateinit var connectionUrl: String

    @BeforeEach
    internal fun setUp() {
        connectionUrl = "nats://" + natsContainer.host + ":" + natsContainer.getMappedPort(4222)
        connection = Nats.connect(connectionUrl)
        Assumptions.assumeTrue(connection.status == Connection.Status.CONNECTED)
    }

    @AfterEach
    internal fun tearDown() {
        connection.close()
    }

    @Test
    fun `test create dictionary success`() = runBlocking {
        val testDictionaryId = "42"
        val testUserId = "fff"
        val testDbDictionary = DbDictionary(
            dictionaryId = testDictionaryId,
            userId = testUserId,
            name = "GGG",
            sourceLang = DbLang("x"),
            targetLang = DbLang("y"),
            details = mapOf("A" to 42, "B" to true),
        )

        val dictionaryRepository = mockk<DbDictionaryRepository>()
        val repositories = mockk<DbRepositories>()
        every {
            repositories.dictionaryRepository
        } returns dictionaryRepository
        every {
            dictionaryRepository.createDictionary(any())
        } returns testDbDictionary

        val processor = DictionariesServerProcessor(
            repositories = repositories,
            topic = "XXX",
            group = "QQQ",
            connectionFactory = { Nats.connect(connectionUrl) },
        )

        DictionariesServerController(processor).start()
        while (!processor.ready()) {
            Thread.sleep(100)
        }

        val context = DictionaryContext(
            operation = DictionaryOperation.CREATE_DICTIONARY,
            requestAppAuthId = AppAuthId(testUserId)
        ).also {
            it.requestDictionaryEntity = DictionaryEntity.EMPTY.copy(
                name = "GGG",
                sourceLang = LangEntity(LangId("src")),
                targetLang = LangEntity(LangId("dst")),
            )
        }
        val answer = connection.request(
            /* subject = */ "XXX",
            /* body = */ context.toByteArray(),
            /* timeout = */ Duration.of(42, ChronoUnit.SECONDS)
        )
        val res = dictionaryContextFromByteArray(answer.data)

        Assertions.assertTrue(res.errors.isEmpty())
        Assertions.assertEquals(DictionaryId("42"), res.responseDictionaryEntity.dictionaryId)
        Assertions.assertEquals("GGG", res.responseDictionaryEntity.name)

        processor.close()
    }
}