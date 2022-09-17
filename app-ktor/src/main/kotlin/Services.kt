package com.gitlab.sszuev.flashcards

import com.gitlab.sszuev.flashcards.services.CardService
import com.gitlab.sszuev.flashcards.services.DictionaryService
import com.gitlab.sszuev.flashcards.services.impl.CardServiceImpl
import com.gitlab.sszuev.flashcards.services.impl.DictionaryServiceImpl

internal fun cardService(): CardService {
    return CardServiceImpl()
}

internal fun dictionaryService(): DictionaryService {
    return DictionaryServiceImpl()
}
