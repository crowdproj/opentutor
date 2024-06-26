package com.gitlab.sszuev.flashcards.core.processes

import com.gitlab.sszuev.flashcards.DictionaryContext
import com.gitlab.sszuev.flashcards.core.documents.createReader
import com.gitlab.sszuev.flashcards.core.documents.createWriter
import com.gitlab.sszuev.flashcards.core.mappers.toCardEntity
import com.gitlab.sszuev.flashcards.core.mappers.toDbCard
import com.gitlab.sszuev.flashcards.core.mappers.toDbDictionary
import com.gitlab.sszuev.flashcards.core.mappers.toDictionaryEntity
import com.gitlab.sszuev.flashcards.core.mappers.toDocumentCard
import com.gitlab.sszuev.flashcards.core.mappers.toDocumentDictionary
import com.gitlab.sszuev.flashcards.core.normalizers.normalize
import com.gitlab.sszuev.flashcards.corlib.ChainDSL
import com.gitlab.sszuev.flashcards.corlib.worker
import com.gitlab.sszuev.flashcards.model.common.AppStatus
import com.gitlab.sszuev.flashcards.model.domain.DictionaryOperation
import com.gitlab.sszuev.flashcards.model.domain.ResourceEntity

fun ChainDSL<DictionaryContext>.processGetAllDictionary() = worker {
    this.name = "process get-all-dictionary request"
    test {
        this.status == AppStatus.RUN
    }
    process {
        val userId = this.normalizedRequestAppAuthId

        if (this.config.createBuiltinDictionariesOnFirstLogin) {
            this.repositories.userRepository.createUserIfAbsent(userId) {
                this.populateBuiltinDictionaries()
            }
        }

        val res = this.repositories.dictionaryRepository
            .findDictionariesByUserId(userId.asString())
            .map { it.toDictionaryEntity().normalize() }.toList()
        this.responseDictionaryEntityList = res.map { dictionary ->
            val cards =
                this.repositories.cardRepository
                    .findCardsByDictionaryId(dictionary.dictionaryId.asString())
                    .toList()
            val total = cards.size
            val known = cards.mapNotNull { it.answered }.count { it >= config.numberOfRightAnswers }
            dictionary.copy(totalCardsCount = total, learnedCardsCount = known)
        }

        this.status = if (this.errors.isNotEmpty()) AppStatus.FAIL else AppStatus.RUN
    }
    onException {
        this.handleThrowable(DictionaryOperation.GET_ALL_DICTIONARIES, it)
    }
}

fun ChainDSL<DictionaryContext>.processCreateDictionary() = worker {
    this.name = "process create-dictionary request"
    test {
        this.status == AppStatus.RUN
    }
    process {
        val userId = this.normalizedRequestAppAuthId
        val res = this.repositories.dictionaryRepository
            .createDictionary(this.normalizedRequestDictionaryEntity.copy(userId = userId).toDbDictionary())
            .toDictionaryEntity().normalize()
        this.responseDictionaryEntity = res
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
        val userId = this.normalizedRequestAppAuthId
        val dictionaryId = this.normalizedRequestDictionaryId
        val dictionary = this.repositories.dictionaryRepository
            .findDictionaryById(dictionaryId.asString())?.toDictionaryEntity()?.normalize()
        if (dictionary == null) {
            this.errors.add(
                noDictionaryFoundDataError(
                    operation = DictionaryOperation.DELETE_DICTIONARY,
                    id = dictionaryId,
                    userId = normalizedRequestAppAuthId
                )
            )
        } else if (dictionary.userId != userId) {
            this.errors.add(forbiddenEntityDataError(DictionaryOperation.DELETE_DICTIONARY, dictionaryId, userId))
        } else {
            this.repositories.dictionaryRepository
                .deleteDictionary(this.normalizedRequestDictionaryId.asString())
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
        val userId = this.normalizedRequestAppAuthId
        val dictionaryId = this.normalizedRequestDictionaryId
        val dictionary = this.repositories.dictionaryRepository
            .findDictionaryById(dictionaryId.asString())?.toDictionaryEntity()?.normalize()
        if (dictionary == null) {
            this.errors.add(
                noDictionaryFoundDataError(
                    operation = DictionaryOperation.DOWNLOAD_DICTIONARY,
                    id = dictionaryId,
                    userId = normalizedRequestAppAuthId
                )
            )
        } else if (dictionary.userId != userId) {
            this.errors.add(forbiddenEntityDataError(DictionaryOperation.DOWNLOAD_DICTIONARY, dictionaryId, userId))
        } else {
            val cards = this.repositories.cardRepository
                .findCardsByDictionaryId(dictionaryId.asString())
                .map { it.toCardEntity() }
                .map { it.toDocumentCard(this.config) }
                .toList()
            val document = dictionary.toDocumentDictionary().copy(cards = cards)
            try {
                val res = createWriter().write(document)
                this.responseDictionaryResourceEntity = ResourceEntity(resourceId = dictionaryId, data = res)
            } catch (ex: Exception) {
                handleThrowable(DictionaryOperation.DOWNLOAD_DICTIONARY, ex)
            }
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
        try {
            val userId = this.normalizedRequestAppAuthId
            val document = createReader().parse(this.requestDictionaryResourceEntity.data)
            val dictionary = this.repositories.dictionaryRepository
                .createDictionary(
                    document.toDictionaryEntity().normalize().copy(userId = userId).toDbDictionary()
                )
                .toDictionaryEntity().normalize()
            val cards = document.cards.asSequence()
                .map { it.toCardEntity(this.config) }
                .map { it.copy(dictionaryId = dictionary.dictionaryId) }
                .map { it.toDbCard() }
                .toList()
            if (cards.isNotEmpty()) {
                this.repositories.cardRepository.createCards(cards)
            }
            this.responseDictionaryEntity = dictionary
        } catch (ex: Exception) {
            handleThrowable(DictionaryOperation.UPLOAD_DICTIONARY, ex)
        }
        this.status = if (this.errors.isNotEmpty()) AppStatus.FAIL else AppStatus.RUN
    }
    onException {
        this.handleThrowable(DictionaryOperation.UPLOAD_DICTIONARY, it)
    }
}