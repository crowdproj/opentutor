package com.gitlab.sszuev.flashcards.settings

import com.gitlab.sszuev.flashcards.DbRepositories
import com.gitlab.sszuev.flashcards.SettingsContext
import com.gitlab.sszuev.flashcards.model.common.AppAuthId
import com.gitlab.sszuev.flashcards.model.domain.SettingsEntity
import com.gitlab.sszuev.flashcards.model.domain.SettingsOperation
import com.gitlab.sszuev.flashcards.repositories.DbUser
import com.gitlab.sszuev.flashcards.repositories.DbUserRepository
import com.gitlab.sszuev.flashcards.utils.settingsContextFromByteArray
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
internal class SettingsServerProcessorTest {
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
    fun `test get settings success`() = runBlocking {
        val testUserId = "fff"
        val testDbUser = DbUser(
            id = testUserId,
            details = mapOf(
                "numberOfWordsPerStage" to 12,
                "stageShowNumberOfWords" to 13,
                "stageOptionsNumberOfVariants" to 14,
            )
        )

        val userRepository = mockk<DbUserRepository>()
        val repositories = mockk<DbRepositories>()
        every {
            repositories.userRepository
        } returns userRepository
        every {
            userRepository.findByUserId(testUserId)
        } returns testDbUser

        val processor = SettingsServerProcessor(
            repositories = repositories,
            topic = "XXX",
            group = "QQQ",
            connectionFactory = { Nats.connect(connectionUrl) },
        )

        SettingsServerController(processor).start()
        while (!processor.ready()) {
            Thread.sleep(100)
        }

        val context = SettingsContext(
            operation = SettingsOperation.GET_SETTINGS,
            requestAppAuthId = AppAuthId(testUserId)
        )
        val answer = connection.request(
            /* subject = */ "XXX",
            /* body = */ context.toByteArray(),
            /* timeout = */ Duration.of(42, ChronoUnit.SECONDS)
        )
        val res = settingsContextFromByteArray(answer.data)

        Assertions.assertTrue(res.errors.isEmpty())
        Assertions.assertEquals(
            SettingsEntity.DEFAULT.copy(
                numberOfWordsPerStage = 12,
                stageShowNumberOfWords = 13,
                stageOptionsNumberOfVariants = 14,
            ),
            res.responseSettingsEntity
        )

        processor.close()
    }

}