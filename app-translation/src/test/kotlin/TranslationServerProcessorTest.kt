package com.gitlab.sszuev.flashcards.translation

import com.gitlab.sszuev.flashcards.TranslationContext
import com.gitlab.sszuev.flashcards.model.common.AppAuthId
import com.gitlab.sszuev.flashcards.model.domain.CardEntity
import com.gitlab.sszuev.flashcards.model.domain.CardWordEntity
import com.gitlab.sszuev.flashcards.model.domain.LangId
import com.gitlab.sszuev.flashcards.model.domain.TTSResourceId
import com.gitlab.sszuev.flashcards.model.domain.TranslationOperation
import com.gitlab.sszuev.flashcards.translation.api.TranslationEntity
import com.gitlab.sszuev.flashcards.translation.api.TranslationRepository
import com.gitlab.sszuev.flashcards.utils.toByteArray
import com.gitlab.sszuev.flashcards.utils.translationContextFromByteArray
import io.mockk.coEvery
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
internal class TranslationServerProcessorTest {
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
    fun `test get translation success`() = runBlocking {
        val testSourceLang = "src"
        val testTargetLang = "dst"
        val testQueryWord = "test-word"
        val testTranslation = "test-translation"
        val testTranslationEntity =
            TranslationEntity(word = testQueryWord, translations = listOf(listOf(testTranslation)))

        val translationRepository = mockk<TranslationRepository>()
        coEvery {
            translationRepository.fetch(sourceLang = testSourceLang, targetLang = testTargetLang, word = testQueryWord)
        } returns listOf(testTranslationEntity)


        val processor = TranslationServerProcessor(
            repository = translationRepository,
            topic = "XXX",
            group = "QQQ",
            connectionFactory = { Nats.connect(connectionUrl) },
        )

        TranslationServerController(processor).start()
        while (!processor.ready()) {
            Thread.sleep(100)
        }

        val context = TranslationContext(
            operation = TranslationOperation.FETCH_CARD,
            requestAppAuthId = AppAuthId("xxx"),
            requestWord = testQueryWord,
            requestSourceLang = LangId(testSourceLang),
            requestTargetLang = LangId(testTargetLang),
        )
        val answer = connection.request(
            /* subject = */ "XXX",
            /* body = */ context.toByteArray(),
            /* timeout = */ Duration.of(42, ChronoUnit.SECONDS)
        )
        val res = translationContextFromByteArray(answer.data)

        Assertions.assertTrue(res.errors.isEmpty())
        Assertions.assertEquals(
            CardEntity.EMPTY.copy(
                words = listOf(
                    CardWordEntity(
                        word = testQueryWord,
                        translations = listOf(listOf(testTranslation)),
                        primary = true,
                        sound = TTSResourceId("$testSourceLang:$testQueryWord")
                    )
                ),
            ),
            res.responseCardEntity
        )

        processor.close()
    }

}