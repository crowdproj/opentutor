package com.gitlab.sszuev.flashcards

import com.gitlab.sszuev.flashcards.services.CardService
import com.gitlab.sszuev.flashcards.services.DictionaryService
import com.gitlab.sszuev.flashcards.services.impl.CardServiceImpl
import com.gitlab.sszuev.flashcards.services.impl.DictionaryServiceImpl

internal fun cardService(repositories: CardRepositories): CardService {
    return CardServiceImpl(repositories)
}

internal fun dictionaryService(repositories: DictionaryRepositories): DictionaryService {
    return DictionaryServiceImpl(repositories)
}
