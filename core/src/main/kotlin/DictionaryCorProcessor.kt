package com.gitlab.sszuev.flashcards.core

import com.gitlab.sszuev.flashcards.DictionaryContext
import com.gitlab.sszuev.flashcards.core.normalizers.normalizers
import com.gitlab.sszuev.flashcards.core.processes.*
import com.gitlab.sszuev.flashcards.core.stubs.dictionaryStubSuccess
import com.gitlab.sszuev.flashcards.core.stubs.stubError
import com.gitlab.sszuev.flashcards.core.validators.*
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

            operation(DictionaryOperation.CREATE_DICTIONARY) {
                normalizers(DictionaryOperation.CREATE_DICTIONARY)
                validators(DictionaryOperation.CREATE_DICTIONARY) {
                    validateUserId(DictionaryOperation.CREATE_DICTIONARY)
                    validateDictionaryEntityHasNoCardId { it.normalizedRequestDictionaryEntity }
                    validateDictionaryLangId("source-lang") { it.normalizedRequestDictionaryEntity.sourceLang.langId }
                    validateDictionaryLangId("target-lang") { it.normalizedRequestDictionaryEntity.targetLang.langId }
                }
                runs(DictionaryOperation.CREATE_DICTIONARY) {
                    processFindUser(DictionaryOperation.CREATE_DICTIONARY)
                    processCreateDictionary()
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

            operation(DictionaryOperation.DOWNLOAD_DICTIONARY) {
                normalizers(DictionaryOperation.DOWNLOAD_DICTIONARY)
                validators(DictionaryOperation.DOWNLOAD_DICTIONARY) {
                    validateUserId(DictionaryOperation.DOWNLOAD_DICTIONARY)
                    validateDictionaryId { (it as DictionaryContext).normalizedRequestDictionaryId }
                }
                runs(DictionaryOperation.DOWNLOAD_DICTIONARY) {
                    processFindUser(DictionaryOperation.DOWNLOAD_DICTIONARY)
                    processDownloadDictionary()
                }
            }

            operation(DictionaryOperation.UPLOAD_DICTIONARY) {
                normalizers(DictionaryOperation.UPLOAD_DICTIONARY)
                validators(DictionaryOperation.UPLOAD_DICTIONARY) {
                    validateUserId(DictionaryOperation.UPLOAD_DICTIONARY)
                    validateDictionaryResource()
                }
                runs(DictionaryOperation.UPLOAD_DICTIONARY) {
                    processFindUser(DictionaryOperation.UPLOAD_DICTIONARY)
                    processUploadDictionary()
                }
            }
        }.build()
    }
}