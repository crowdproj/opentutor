package com.gitlab.sszuev.flashcards.core.processes

import com.gitlab.sszuev.flashcards.TranslationContext
import com.gitlab.sszuev.flashcards.core.mappers.toCardEntity
import com.gitlab.sszuev.flashcards.core.normalizers.normalize
import com.gitlab.sszuev.flashcards.corlib.ChainDSL
import com.gitlab.sszuev.flashcards.corlib.worker
import com.gitlab.sszuev.flashcards.model.common.AppStatus
import com.gitlab.sszuev.flashcards.model.domain.TranslationOperation

fun ChainDSL<TranslationContext>.processTranslation() = worker {
    this.name = "process fetch translation request"
    process {
        val query = this.normalizedRequestWord
        val sourceLang = this.normalizedRequestSourceLang.asString()
        val targetLang = this.normalizedRequestTargetLang.asString()

        val found =
            this.repository.fetch(sourceLang = sourceLang, targetLang = targetLang, word = query)?.toCardEntity()
        if (found == null) {
            this.errors.add(
                runError(
                    operation = TranslationOperation.FETCH_CARD,
                    fieldName = "($sourceLang -> $targetLang):::$query",
                    description = "no card fetched."
                )
            )
            this.status = AppStatus.FAIL
        } else {
            this.responseCardEntity = found.normalize()
            this.status = AppStatus.RUN
        }
    }
    onException {
        val query = this.normalizedRequestWord
        val sourceLang = this.normalizedRequestSourceLang.asString()
        val targetLang = this.normalizedRequestTargetLang.asString()
        fail(
            runError(
                operation = TranslationOperation.FETCH_CARD,
                fieldName = "($sourceLang -> $targetLang):::$query",
                description = "unexpected exception",
                exception = it
            )
        )
    }
}