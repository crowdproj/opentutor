package com.gitlab.sszuev.flashcards.core.mappers

import com.gitlab.sszuev.flashcards.model.domain.DictionaryEntity
import com.gitlab.sszuev.flashcards.model.domain.DocumentEntity

val DocumentEntity.dictionary: DictionaryEntity
    get() = DictionaryEntity(
        name = this.name,
        sourceLang = this.sourceLang,
        targetLang = this.targetLang,
        numberOfRightAnswers = this.numberOfRightAnswers,
    )

val DictionaryEntity.document: DocumentEntity
    get() = DocumentEntity(
        name = this.name,
        sourceLang = this.sourceLang,
        targetLang = this.targetLang,
        numberOfRightAnswers = this.numberOfRightAnswers,
    )