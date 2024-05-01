package com.gitlab.sszuev.flashcards

import com.gitlab.sszuev.flashcards.config.RunConfig
import com.gitlab.sszuev.flashcards.dbmem.MemDbCardRepository
import com.gitlab.sszuev.flashcards.dbmem.MemDbDictionaryRepository
import com.gitlab.sszuev.flashcards.dbpg.PgDbCardRepository
import com.gitlab.sszuev.flashcards.dbpg.PgDbDictionaryRepository
import com.gitlab.sszuev.flashcards.speaker.NatsTTSResourceRepository
import com.gitlab.sszuev.flashcards.speaker.createDirectTTSResourceRepository

fun appRepositories(config: RunConfig) = if (config.mode == RunConfig.Mode.TEST) {
    AppRepositories(
        cardRepository = MemDbCardRepository(),
        dictionaryRepository = MemDbDictionaryRepository(),
        ttsClientRepository = createDirectTTSResourceRepository(),
    )
} else {
    AppRepositories(
        cardRepository = PgDbCardRepository(),
        dictionaryRepository = PgDbDictionaryRepository(),
        ttsClientRepository = NatsTTSResourceRepository(),
    )
}