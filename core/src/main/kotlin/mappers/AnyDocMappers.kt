package com.gitlab.sszuev.flashcards.core.mappers

import com.gitlab.sszuev.flashcards.model.domain.DictionaryEntity
import com.gitlab.sszuev.flashcards.model.domain.DocumentEntity

val DocumentEntity.dictionary: DictionaryEntity
    get() = DictionaryEntity(
        name = name,
        sourceLang = sourceLang,
        targetLang = targetLang,
    )

val DictionaryEntity.document: DocumentEntity
    get() = DocumentEntity(
        name = name,
        sourceLang = sourceLang,
        targetLang = targetLang,
    )