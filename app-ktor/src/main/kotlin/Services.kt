package com.gitlab.sszuev.flashcards

import com.gitlab.sszuev.flashcards.services.CardService
import com.gitlab.sszuev.flashcards.services.DictionaryService

internal fun cardService(): CardService {
    return CardService()
}

internal fun dictionaryService(): DictionaryService {
    return DictionaryService()
}
