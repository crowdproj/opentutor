package com.gitlab.sszuev.flashcards

import com.gitlab.sszuev.flashcards.services.CardService
import com.gitlab.sszuev.flashcards.services.impl.CardServiceImpl

fun cardService(conf: AppConfig): CardService {
    return CardServiceImpl(CardContextRepositories(ttsClient = conf.ttsClientRepositoryImpl))
}
