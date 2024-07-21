package com.gitlab.sszuev.flashcards.core.processes

import com.gitlab.sszuev.flashcards.DictionaryContext
import com.gitlab.sszuev.flashcards.core.documents.createReader
import com.gitlab.sszuev.flashcards.core.documents.createWriter
import com.gitlab.sszuev.flashcards.core.mappers.dictionary
import com.gitlab.sszuev.flashcards.core.mappers.document
import com.gitlab.sszuev.flashcards.core.mappers.toCardEntity
import com.gitlab.sszuev.flashcards.core.mappers.toDbCard
import com.gitlab.sszuev.flashcards.core.mappers.toDbDictionary
import com.gitlab.sszuev.flashcards.core.mappers.toDictionaryEntity
import com.gitlab.sszuev.flashcards.core.mappers.toXmlDocumentCard
import com.gitlab.sszuev.flashcards.core.mappers.toXmlDocumentDictionary
import com.gitlab.sszuev.flashcards.core.normalizers.normalize
import com.gitlab.sszuev.flashcards.corlib.ChainDSL
import com.gitlab.sszuev.flashcards.corlib.worker
import com.gitlab.sszuev.flashcards.model.common.AppStatus
import com.gitlab.sszuev.flashcards.model.domain.CardId
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
import com.gitlab.sszuev.flashcards.model.domain.DictionaryOperation
import com.gitlab.sszuev.flashcards.model.domain.ResourceEntity
import com.gitlab.sszuev.flashcards.utils.documentEntityFromJson
import com.gitlab.sszuev.flashcards.utils.toJsonString

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

        val dictionaries = this.repositories.dictionaryRepository
            .findDictionariesByUserId(userId.asString())
            .map { it.toDictionaryEntity().normalize() }.toList()
        val cardCounts = this.repositories.cardRepository.countCardsByDictionaryId(
            dictionaries.map { it.dictionaryId.asString() }
        )
        val answeredCounts = this.repositories.cardRepository.countCardsByDictionaryIdAndAnswered(
            dictionaries.map { it.dictionaryId.asString() },
            config.numberOfRightAnswers
        )

        this.responseDictionaryEntityList = dictionaries.map { dictionary ->
            val total = cardCounts[dictionary.dictionaryId.asString()] ?: 0
            val known = answeredCounts[dictionary.dictionaryId.asString()] ?: 0
            dictionary.copy(totalCardsCount = total.toInt(), learnedCardsCount = known.toInt())
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
            when (this.normalizedRequestDownloadDocumentType) {
                "xml" -> {

                    val cards = this.repositories.cardRepository
                        .findCardsByDictionaryId(dictionaryId.asString())
                        .map { it.toCardEntity() }
                        .map { it.toXmlDocumentCard(this.config) }
                        .toList()
                    val document = dictionary.toXmlDocumentDictionary().copy(cards = cards)
                    try {
                        val res = createWriter().write(document)
                        this.responseDictionaryResourceEntity = ResourceEntity(resourceId = dictionaryId, data = res)
                    } catch (ex: Exception) {
                        handleThrowable(DictionaryOperation.DOWNLOAD_DICTIONARY, ex)
                    }
                }

                "json" -> {
                    val document = dictionary.document
                    val cards = this.repositories.cardRepository
                        .findCardsByDictionaryId(dictionaryId.asString())
                        .map { it.toCardEntity() }
                        .map { it.copy(dictionaryId = DictionaryId.NONE, cardId = CardId.NONE) }
                        .toList()
                    try {
                        val res = document.copy(cards = cards).toJsonString().toByteArray(Charsets.UTF_8)
                        this.responseDictionaryResourceEntity = ResourceEntity(resourceId = dictionaryId, data = res)
                    } catch (ex: Exception) {
                        handleThrowable(DictionaryOperation.DOWNLOAD_DICTIONARY, ex)
                    }
                }

                else -> {
                    errors.add(
                        dataError(
                            operation = DictionaryOperation.DOWNLOAD_DICTIONARY,
                            fieldName = dictionaryId.asString(),
                            details = "Unsupported type: '${this.normalizedRequestDownloadDocumentType}'",
                        )
                    )
                }
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
        val userId = this.normalizedRequestAppAuthId
        when (this.normalizedRequestDownloadDocumentType) {
            "xml" -> {
                try {
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
            }

            "json" -> {
                try {
                    val document =
                        documentEntityFromJson(this.requestDictionaryResourceEntity.data.toString(Charsets.UTF_8))
                    val dictionary = this.repositories.dictionaryRepository
                        .createDictionary(
                            document.dictionary.normalize().copy(userId = userId).toDbDictionary()
                        )
                        .toDictionaryEntity().normalize()
                    val cards =
                        document.cards.map { it.copy(dictionaryId = dictionary.dictionaryId) }.map { it.toDbCard() }
                    if (cards.isNotEmpty()) {
                        this.repositories.cardRepository.createCards(cards)
                    }
                    this.responseDictionaryEntity = dictionary
                } catch (ex: Exception) {
                    handleThrowable(DictionaryOperation.UPLOAD_DICTIONARY, ex)
                }
            }

            else -> {
                errors.add(
                    dataError(
                        operation = DictionaryOperation.UPLOAD_DICTIONARY,
                        details = "Unsupported type: '${this.normalizedRequestDownloadDocumentType}'",
                    )
                )
            }
        }
        this.status = if (this.errors.isNotEmpty()) AppStatus.FAIL else AppStatus.RUN
    }
    onException {
        this.handleThrowable(DictionaryOperation.UPLOAD_DICTIONARY, it)
    }
}