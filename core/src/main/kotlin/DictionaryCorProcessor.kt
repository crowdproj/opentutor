package com.gitlab.sszuev.flashcards.core

import com.gitlab.sszuev.flashcards.DictionaryContext
import com.gitlab.sszuev.flashcards.core.normalizers.normalizers
import com.gitlab.sszuev.flashcards.core.processes.processDeleteDictionary
import com.gitlab.sszuev.flashcards.core.processes.processFindUser
import com.gitlab.sszuev.flashcards.core.processes.processGetAllDictionary
import com.gitlab.sszuev.flashcards.core.stubs.dictionaryStubSuccess
import com.gitlab.sszuev.flashcards.core.stubs.stubError
import com.gitlab.sszuev.flashcards.core.validators.validateDictionaryId
import com.gitlab.sszuev.flashcards.core.validators.validateUserId
import com.gitlab.sszuev.flashcards.corlib.chain
import com.gitlab.sszuev.flashcards.model.domain.DictionaryOperation
import com.gitlab.sszuev.flashcards.stubs.stubDictionaries

class DictionaryCorProcessor {
    suspend fun execute(context: DictionaryContext) = businessChain.exec(context)

    companion object {
        private val businessChain = chain {
            name = "DictionaryContext Root Chain"
            initContext()

            operation(DictionaryOperation.GET_ALL_DICTIONARIES) {
                stubs(DictionaryOperation.GET_ALL_DICTIONARIES) {
                    dictionaryStubSuccess(DictionaryOperation.GET_ALL_DICTIONARIES) {
                        this.responseDictionaryEntityList = stubDictionaries
                    }
                    stubError(DictionaryOperation.GET_ALL_DICTIONARIES)
                }
                normalizers(DictionaryOperation.GET_ALL_DICTIONARIES)
                validators(DictionaryOperation.GET_ALL_DICTIONARIES) {
                }
                runs(DictionaryOperation.GET_ALL_DICTIONARIES) {
                    processFindUser(DictionaryOperation.GET_ALL_DICTIONARIES)
                    processGetAllDictionary()
                }
            }

            operation(DictionaryOperation.DELETE_DICTIONARY) {
                normalizers(DictionaryOperation.DELETE_DICTIONARY)
                validators(DictionaryOperation.DELETE_DICTIONARY) {
                    validateUserId(DictionaryOperation.DELETE_DICTIONARY)
                    validateDictionaryId { (it as DictionaryContext).normalizedRequestDictionaryId }
                }
                runs(DictionaryOperation.DELETE_DICTIONARY) {
                    processFindUser(DictionaryOperation.DELETE_DICTIONARY)
                    processDeleteDictionary()
                }
            }
        }.build()
    }
}