package com.gitlab.sszuev.flashcards.core

import com.gitlab.sszuev.flashcards.TTSContext
import com.gitlab.sszuev.flashcards.core.normalizers.normalizers
import com.gitlab.sszuev.flashcards.core.processes.processResource
import com.gitlab.sszuev.flashcards.core.validators.validateResourceGetLangId
import com.gitlab.sszuev.flashcards.core.validators.validateResourceGetWord
import com.gitlab.sszuev.flashcards.core.validators.validateUserId
import com.gitlab.sszuev.flashcards.corlib.chain
import com.gitlab.sszuev.flashcards.model.domain.TTSOperation

class TTSCorProcessor {

    suspend fun execute(context: TTSContext) = businessChain.exec(context)

    companion object {
        private val businessChain = chain {
            name = "TTS Root Chain"
            initContext()

            operation(TTSOperation.GET_RESOURCE) {
                normalizers(TTSOperation.GET_RESOURCE)
                validators(TTSOperation.GET_RESOURCE) {
                    validateUserId(TTSOperation.GET_RESOURCE)
                    validateResourceGetLangId()
                    validateResourceGetWord()
                }
                runs(TTSOperation.GET_RESOURCE) {
                    processResource()
                }
            }
        }.build()
    }
}