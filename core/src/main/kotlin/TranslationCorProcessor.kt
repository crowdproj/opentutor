package com.gitlab.sszuev.flashcards.core

import com.gitlab.sszuev.flashcards.TranslationContext
import com.gitlab.sszuev.flashcards.core.normalizers.normalizers
import com.gitlab.sszuev.flashcards.core.processes.processTranslation
import com.gitlab.sszuev.flashcards.core.validators.validateLangId
import com.gitlab.sszuev.flashcards.core.validators.validateQueryWord
import com.gitlab.sszuev.flashcards.core.validators.validateUserId
import com.gitlab.sszuev.flashcards.corlib.chain
import com.gitlab.sszuev.flashcards.model.domain.TranslationOperation

class TranslationCorProcessor {

    suspend fun execute(context: TranslationContext) = businessChain.exec(context)

    companion object {
        private val businessChain = chain {
            name = "Translation Root Chain"
            initContext()

            operation(TranslationOperation.FETCH_CARD) {
                normalizers(TranslationOperation.FETCH_CARD)
                validators(TranslationOperation.FETCH_CARD) {
                    validateUserId(TranslationOperation.FETCH_CARD)
                    validateLangId("source-lang") {
                        it.normalizedRequestSourceLang
                    }
                    validateLangId("target-lang") {
                        it.normalizedRequestSourceLang
                    }
                    validateQueryWord()
                }
                runs(TranslationOperation.FETCH_CARD) {
                    processTranslation()
                }
            }
        }.build()
    }
}