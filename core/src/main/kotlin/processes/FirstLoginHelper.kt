package com.gitlab.sszuev.flashcards.core.processes

import com.github.benmanes.caffeine.cache.Caffeine
import com.gitlab.sszuev.flashcards.DictionaryContext
import com.gitlab.sszuev.flashcards.core.mappers.dictionary
import com.gitlab.sszuev.flashcards.core.mappers.toDbCard
import com.gitlab.sszuev.flashcards.core.mappers.toDbDictionary
import com.gitlab.sszuev.flashcards.model.common.AppAuthId
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
import com.gitlab.sszuev.flashcards.model.domain.DocumentEntity
import com.gitlab.sszuev.flashcards.repositories.DbUser
import com.gitlab.sszuev.flashcards.repositories.DbUserRepository
import com.gitlab.sszuev.flashcards.utils.documentEntityFromJson
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("com.gitlab.sszuev.flashcards.core.processes.FirstLoginHelper")

private val users = Caffeine.newBuilder().maximumSize(1024).build<AppAuthId, AppAuthId>()

internal fun DbUserRepository.createUserIfAbsent(id: AppAuthId, onCreate: () -> Unit) {
    if (users.getIfPresent(id) != null) {
        return
    }
    users.put(id, id)
    if (findByUserId(id.asString()) != null) {
        return
    }
    logger.info("Create user $id")
    createUser(DbUser(id = id.asString()))
    onCreate()
}

internal fun DictionaryContext.populateBuiltinDictionaries() {
    if (!this.config.createBuiltinDictionariesOnFirstLogin) {
        return
    }
    val userId = this.normalizedRequestAppAuthId
    val documents = loadBuiltinDocuments().toList()

    documents.forEach { document ->
        val dictionary = document.dictionary.copy(userId = userId).toDbDictionary()
        logger.info("Create dictionary '${dictionary.name}'")
        val id = DictionaryId(this.repositories.dictionaryRepository.createDictionary(dictionary).dictionaryId)
        val cards = document.cards.asSequence().map { it.copy(dictionaryId = id) }.map { it.toDbCard() }.toList()
        logger.info("Dictionary '${dictionary.name}': id = $id, cards = ${cards.size}")
        this.repositories.cardRepository.createCards(cards)
    }
}

internal fun loadBuiltinDocuments(): Sequence<DocumentEntity> =
    sequenceOf("/irregular-verbs-en-ru.json", "/weather-en-ru.json").map {
        checkNotNull(object {}.javaClass.getResourceAsStream(it)) { "Can't find resource $it" }
            .bufferedReader(Charsets.UTF_8)
            .readText()
    }.map {
        documentEntityFromJson(it)
    }
