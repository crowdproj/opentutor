package com.gitlab.sszuev.flashcards

import com.gitlab.sszuev.flashcards.services.CardService
import com.gitlab.sszuev.flashcards.services.impl.CardServiceImpl

internal fun cardService(repositories: CardRepositories): CardService {
    return CardServiceImpl(repositories)
}
