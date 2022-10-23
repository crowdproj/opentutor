package com.gitlab.sszuev.flashcards.speaker.impl

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectory

internal class LocalTextToSpeechServiceTest {

    @Test
    fun `test load from classpath`() {
        val res = LocalTextToSpeechService.load("classpath:/tts-test-data")
        val libraries = (res as LocalTextToSpeechService).libraries
        Assertions.assertEquals(1, libraries.size)
        Assertions.assertNotNull(libraries["en"])
    }

    @Test
    fun `test load from directory`(@TempDir dir: Path) {
        val lang1 = dir.resolve("ex").createDirectory()
        val lang2 = dir.resolve("xe").createDirectory()
        Files.createTempFile(lang1, "a", ".tar")
        Files.createTempFile(lang1, "b", ".tar")
        Files.createTempFile(lang2, "a", ".tar")
        val res = LocalTextToSpeechService.load(dir.toString())
        val libraries = (res as LocalTextToSpeechService).libraries
        Assertions.assertEquals(2, libraries.size)
        Assertions.assertEquals(2, libraries["ex"]!!.size)
        Assertions.assertEquals(1, libraries["xe"]!!.size)
    }
}