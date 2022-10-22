package com.gitlab.sszuev.flashcards.api.testutils

import com.gitlab.sszuev.flashcards.api.v1.models.*
import org.junit.jupiter.api.Assertions

internal val dictionary = DictionaryResource(
    dictionaryId = "42",
    name = "XXX",
    sourceLang = "X",
    targetLang = "Y",
    partsOfSpeech = listOf("X", "Y"),
    total = 1,
    learned = 42
)

internal val error = ErrorResource(
    code = "XXX",
    group = "QQQ",
    field = "VVV",
    message = "mmm"
)

internal val debug = DebugResource(
    mode = RunMode.TEST,
    stub = DebugStub.ERROR_UNKNOWN
)

internal fun assertError(json: String) {
    Assertions.assertTrue(json.contains("\"code\":\"XXX\""))
    Assertions.assertTrue(json.contains("\"group\":\"QQQ\""))
    Assertions.assertTrue(json.contains("\"field\":\"VVV\""))
    Assertions.assertTrue(json.contains("\"message\":\"mmm\""))
}

internal fun assertDebug(json: String) {
    Assertions.assertTrue(json.contains("\"mode\":\"test\""))
    Assertions.assertTrue(json.contains("\"stub\":\"error_unknown\""))
}

internal fun assertDictionary(json: String) {
    Assertions.assertTrue(json.contains("\"dictionaryId\":\"42\""))
    Assertions.assertTrue(json.contains("\"name\":\"XXX\""))
    Assertions.assertTrue(json.contains("\"sourceLang\":\"X\""))
    Assertions.assertTrue(json.contains("\"targetLang\":\"Y\""))
    Assertions.assertTrue(json.contains("\"partsOfSpeech\":[\"X\",\"Y\"]"))
    Assertions.assertTrue(json.contains("\"total\":1"))
    Assertions.assertTrue(json.contains("\"learned\":42"))
}
