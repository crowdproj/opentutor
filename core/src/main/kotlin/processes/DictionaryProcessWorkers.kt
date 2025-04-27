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
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("com.gitlab.sszuev.flashcards.core.processes.DictionaryProcessWorkersKt")

fun ChainDSL<DictionaryContext>.processGetAllDictionary() = worker {
    this.name = "process get-all-dictionary request"
    test {
        this.status == AppStatus.RUN
    }
    process {
        val userId = this.normalizedRequestAppAuthId

        val language = this.userLanguage
        if (language == null) {
            if (logger.isDebugEnabled)
                logger.debug("GetAllDictionaries: no language specified")
            this.repositories.userRepository.getOrCreateUser(id = userId)
        } else {
            if (logger.isDebugEnabled)
                logger.debug("GetAllDictionaries: language = $language")
            this.repositories.userRepository.createOrUpdateUser(id = userId, language = language) {
                logger.info("GetAllDictionaries: populate builtin dictionaries for language $language")
                this.populateBuiltinDictionaries(language)
            }
        }

        val dictionaries = this.repositories.dictionaryRepository
            .findDictionariesByUserId(userId.asString())
            .map { it.toDictionaryEntity(config).normalize() }.toList()
        val cardCounts = this.repositories.cardRepository.countCardsByDictionaryId(
            dictionaries.map { it.dictionaryId.asString() }
        )
        val answeredCounts = mutableMapOf<String, Long>()
        dictionaries.groupBy { it.numberOfRightAnswers }.forEach { (threshold, dictionaries) ->
            answeredCounts.putAll(
                this.repositories.cardRepository.countCardsByDictionaryIdAndAnswered(
                    dictionaries.map { it.dictionaryId.asString() },
                    threshold,
                )
            )
        }

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
            .toDictionaryEntity(config).normalize()
        this.responseDictionaryEntity = res
        this.status = if (this.errors.isNotEmpty()) AppStatus.FAIL else AppStatus.RUN
    }
    onException {
        this.handleThrowable(DictionaryOperation.CREATE_DICTIONARY, it)
    }
}

fun ChainDSL<DictionaryContext>.processUpdateDictionary() = worker {
    this.name = "process update-dictionary request"
    test {
        this.status == AppStatus.RUN
    }
    process {
        val userId = this.normalizedRequestAppAuthId
        val res = this.repositories.dictionaryRepository
            .updateDictionary(this.normalizedRequestDictionaryEntity.copy(userId = userId).toDbDictionary())
            .toDictionaryEntity(config).normalize()
        this.responseDictionaryEntity = res
        this.status = if (this.errors.isNotEmpty()) AppStatus.FAIL else AppStatus.RUN
    }
    onException {
        this.handleThrowable(DictionaryOperation.UPDATE_DICTIONARY, it)
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
            .findDictionaryById(dictionaryId.asString())?.toDictionaryEntity(config)?.normalize()
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
            .findDictionaryById(dictionaryId.asString())?.toDictionaryEntity(config)?.normalize()
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
                val xmlDocument = createReader().parse(this.requestDictionaryResourceEntity.data)
                val dbDictionary = xmlDocument.toDictionaryEntity().normalize().copy(userId = userId).toDbDictionary()
                val dbCards = xmlDocument.cards.asSequence()
                    .map { it.toCardEntity(this.config) }
                    .map { it.toDbCard() }
                    .toList()
                val dictionaryId = this.repositories.documentRepository.save(
                    dictionary = dbDictionary,
                    cards = dbCards,
                )
                this.responseDictionaryEntity =
                    dbDictionary.toDictionaryEntity(config).copy(dictionaryId = DictionaryId(dictionaryId))
            }

            "json" -> {
                val jsonDocument =
                    documentEntityFromJson(this.requestDictionaryResourceEntity.data.toString(Charsets.UTF_8))
                val dbDictionary = jsonDocument.dictionary.normalize().copy(userId = userId).toDbDictionary()
                val dbCards = jsonDocument.cards.map { it.toDbCard() }
                val dictionaryId = this.repositories.documentRepository.save(
                    dictionary = dbDictionary,
                    cards = dbCards,
                )
                this.responseDictionaryEntity =
                    dbDictionary.toDictionaryEntity(config).copy(dictionaryId = DictionaryId(dictionaryId))
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