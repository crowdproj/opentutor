package com.gitlab.sszuev.flashcards.core.processes

import com.gitlab.sszuev.flashcards.DictionaryContext
import com.gitlab.sszuev.flashcards.core.validators.fail
import com.gitlab.sszuev.flashcards.corlib.ChainDSL
import com.gitlab.sszuev.flashcards.corlib.worker
import com.gitlab.sszuev.flashcards.model.common.AppStatus
import com.gitlab.sszuev.flashcards.model.domain.DictionaryOperation

fun ChainDSL<DictionaryContext>.processGetAllDictionary() = worker {
    this.name = "process get-all-dictionary request"
    test {
        this.status == AppStatus.RUN
    }
    process {
        val userId = this.contextUserEntity.id
        val res = this.repositories.dictionaryRepository(this.workMode).getAllDictionaries(userId)
        if (res.errors.isNotEmpty()) {
            this.errors.addAll(res.errors)
        }

        // TODO: temporary solution
        if (res.errors.isEmpty()) {
            this.responseDictionaryEntityList = res.dictionaries.map { dictionary ->
                val cards = this.repositories.cardRepository(this.workMode).getAllCards(userId, dictionary.dictionaryId)
                val total = cards.cards.size
                val known = cards.cards.mapNotNull { it.answered }.count { it >= config.numberOfRightAnswers }
                dictionary.copy(totalCardsCount = total, learnedCardsCount = known)
            }
        }

        this.status = if (this.errors.isNotEmpty()) AppStatus.FAIL else AppStatus.RUN
    }
    onException {
        fail(
            runError(
                operation = DictionaryOperation.GET_ALL_DICTIONARIES,
                fieldName = this.contextUserEntity.id.toFieldName(),
                description = "exception",
                exception = it
            )
        )
    }
}

fun ChainDSL<DictionaryContext>.processCreateDictionary() = worker {
    this.name = "process create-dictionary request"
    test {
        this.status == AppStatus.RUN
    }
    process {
        val userId = this.contextUserEntity.id
        val res =
            this.repositories.dictionaryRepository(this.workMode).createDictionary(userId, this.normalizedRequestDictionaryEntity)
        this.responseDictionaryEntity = res.dictionary
        if (res.errors.isNotEmpty()) {
            this.errors.addAll(res.errors)
        }
        this.status = if (this.errors.isNotEmpty()) AppStatus.FAIL else AppStatus.RUN
    }
    onException {
        this.handleThrowable(DictionaryOperation.CREATE_DICTIONARY, it)
    }
}

fun ChainDSL<DictionaryContext>.processDeleteDictionary() = worker {
    this.name = "process delete-dictionary request"
    test {
        this.status == AppStatus.RUN
    }
    process {
        val userId = this.contextUserEntity.id
        val res =
            this.repositories.dictionaryRepository(this.workMode).removeDictionary(userId, this.normalizedRequestDictionaryId)
        if (res.errors.isNotEmpty()) {
            this.errors.addAll(res.errors)
        }
        this.status = if (this.errors.isNotEmpty()) AppStatus.FAIL else AppStatus.RUN
    }
    onException {
        this.handleThrowable(DictionaryOperation.DELETE_DICTIONARY, it)
    }
}

fun ChainDSL<DictionaryContext>.processDownloadDictionary() = worker {
    this.name = "process download-dictionary request"
    test {
        this.status == AppStatus.RUN
    }
    process {
        val userId = this.contextUserEntity.id
        val res =
            this.repositories.dictionaryRepository(this.workMode).importDictionary(userId, this.normalizedRequestDictionaryId)
        this.responseDictionaryResourceEntity = res.resource
        if (res.errors.isNotEmpty()) {
            this.errors.addAll(res.errors)
        }
        this.status = if (this.errors.isNotEmpty()) AppStatus.FAIL else AppStatus.RUN
    }
    onException {
        this.handleThrowable(DictionaryOperation.DOWNLOAD_DICTIONARY, it)
    }
}

fun ChainDSL<DictionaryContext>.processUploadDictionary() = worker {
    this.name = "process upload-dictionary request"
    test {
        this.status == AppStatus.RUN
    }
    process {
        val res = this.repositories.dictionaryRepository(this.workMode).exportDictionary(
            userId = this.contextUserEntity.id,
            resource = this.requestDictionaryResourceEntity
        )
        this.responseDictionaryEntity = res.dictionary
        if (res.errors.isNotEmpty()) {
            this.errors.addAll(res.errors)
        }
        this.status = if (this.errors.isNotEmpty()) AppStatus.FAIL else AppStatus.RUN
    }
    onException {
        this.handleThrowable(DictionaryOperation.UPLOAD_DICTIONARY, it)
    }
}