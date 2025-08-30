package com.gitlab.sszuev.flashcards.cards

import com.gitlab.sszuev.flashcards.CardContext
import com.gitlab.sszuev.flashcards.DbRepositories
import com.gitlab.sszuev.flashcards.model.common.AppAuthId
import com.gitlab.sszuev.flashcards.model.common.NONE
import com.gitlab.sszuev.flashcards.model.domain.CardId
import com.gitlab.sszuev.flashcards.model.domain.CardOperation
import com.gitlab.sszuev.flashcards.nats.NatsServerProcessor
import com.gitlab.sszuev.flashcards.repositories.DbCard
import com.gitlab.sszuev.flashcards.repositories.DbCardRepository
import com.gitlab.sszuev.flashcards.repositories.DbDictionary
import com.gitlab.sszuev.flashcards.repositories.DbDictionaryRepository
import com.gitlab.sszuev.flashcards.utils.cardContextFromByteArray
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
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Timeout(value = 60, unit = TimeUnit.SECONDS)
@Testcontainers
class CardsServerProcessorTest {
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

    @OptIn(ExperimentalTime::class)
    @Test
    fun `test get card success`() = runBlocking {
        val testCardId = "42"
        val testDictionaryId = "21"
        val testUserId = "uuu"
        val testDbCard = DbCard(
            cardId = testCardId,
            dictionaryId = testDictionaryId,
            changedAt = Instant.NONE,
            details = emptyMap(),
            stats = emptyMap(),
            answered = null,
            words = listOf(
                DbCard.Word(
                    word = "weather",
                    transcription = "'weðə",
                    partOfSpeech = "noun",
                    translations = listOf(listOf("погода")),
                    examples = listOf(
                        DbCard.Word.Example(text = "weather forecast", translation = "прогноз погоды"),
                    ),
                    primary = true,
                ),
            ),
        )
        val testDbDictionary = DbDictionary.NULL.copy(
            dictionaryId = testDictionaryId,
            userId = testUserId,
        )
        val cardRepository = mockk<DbCardRepository>()
        val dictionaryRepository = mockk<DbDictionaryRepository>()
        val repositories = mockk<DbRepositories>()
        every {
            repositories.cardRepository
        } returns cardRepository
        every {
            repositories.dictionaryRepository
        } returns dictionaryRepository
        every {
            cardRepository.findCardById(testCardId)
        } returns testDbCard
        every {
            dictionaryRepository.findDictionaryById(testDictionaryId)
        } returns testDbDictionary

        val processor = NatsServerProcessor(
            topic = "XXX",
            group = "QQQ",
            connection = Nats.connect(connectionUrl),
            parallelism = 8,
            messageHandler = CardsMessageHandler(repositories)
        )

        processor.process()
        while (!processor.ready()) {
            Thread.sleep(100)
        }

        val context = CardContext(operation = CardOperation.GET_CARD, requestAppAuthId = AppAuthId(testUserId)).also {
            it.requestCardEntityId = CardId(testCardId)
        }
        val answer = connection.request(
            /* subject = */ "XXX",
            /* body = */ context.toByteArray(),
            /* timeout = */ Duration.of(42, ChronoUnit.SECONDS)
        )
        val res = cardContextFromByteArray(answer!!.data)

        Assertions.assertTrue(res.errors.isEmpty())
        Assertions.assertEquals(testCardId, res.responseCardEntity.cardId.asString())
        Assertions.assertEquals(testDictionaryId, res.responseCardEntity.dictionaryId.asString())
        Assertions.assertEquals("weather", res.responseCardEntity.words.single().word)

        processor.close()
    }

}