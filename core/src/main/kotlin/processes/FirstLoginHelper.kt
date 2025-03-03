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

private const val DEFAULT_TARGET_LANGUAGE = "zh"
private val RESOURCE_DOCUMENTS_BY_LANGUAGE = mapOf(
    "ru" to listOf("/irregular-verbs-en-ru.json", "/weather-en-ru.json"),
    "en" to listOf("/weather-zh-en.json"),
    "zh" to listOf("/weather-en-zh.json"),
    "es" to listOf("/weather-en-es.json"),
    "pt" to listOf("/weather-en-pt.json"),
    "ja" to listOf("/weather-en-ja.json"),
    "de" to listOf("/weather-en-de.json"),
    "fr" to listOf("/weather-en-fr.json"),
    "it" to listOf("/weather-en-it.json"),
    "pl" to listOf("/weather-en-pl.json"),
    "nl" to listOf("/weather-en-nl.json"),
    "el" to listOf("/weather-en-el.json"),
    "hu" to listOf("/weather-en-hu.json"),
    "cs" to listOf("/weather-en-cs.json"),
    "sv" to listOf("/weather-en-sv.json"),
    "bg" to listOf("/weather-en-bg.json"),
    "da" to listOf("/weather-en-da.json"),
    "fi" to listOf("/weather-en-fi.json"),
    "sk" to listOf("/weather-en-sk.json"),
    "lt" to listOf("/weather-en-lt.json"),
    "lv" to listOf("/weather-en-lv.json"),
    "sl" to listOf("/weather-en-sl.json"),
    "et" to listOf("/weather-en-et.json"),
    "mt" to listOf("/weather-en-mt.json"),
)

internal val users = Caffeine.newBuilder().maximumSize(1024).build<AppAuthId, DbUser>()

internal fun DbUserRepository.createOrUpdateUser(id: AppAuthId, language: String, onCreateOrUpdate: () -> Unit) {
    var dbUser = users.getIfPresent(id)

    if (dbUser != null && dbUser.details.containsKey("language")) {
        return
    }
    dbUser = findOrCreateUser(id.asString(), mapOf("language" to language), onCreateOrUpdate)

    if (!dbUser.details.containsKey("language")) {
        dbUser = addUserDetails(id.asString(), mapOf("language" to language))
        onCreateOrUpdate()
    }

    users.put(id, dbUser)
}

internal fun DbUserRepository.getOrCreateUser(id: AppAuthId): DbUser {
    var dbUser = users.getIfPresent(id)
    if (dbUser != null) {
        return dbUser
    }

    dbUser = findOrCreateUser(id.asString())

    users.put(id, dbUser)
    return dbUser
}

internal fun DbUserRepository.putUser(id: AppAuthId, dbUser: DbUser) {
    users.put(id, this.updateUser(dbUser))
}

internal fun DictionaryContext.populateBuiltinDictionaries(language: String) {
    val userId = this.normalizedRequestAppAuthId
    val documents = loadBuiltinDocuments(language).toList()

    documents.forEach { document ->
        val dictionary = document.dictionary.copy(userId = userId).toDbDictionary()
        logger.info("Create dictionary '${dictionary.name}'")
        val id = DictionaryId(this.repositories.dictionaryRepository.createDictionary(dictionary).dictionaryId)
        val cards = document.cards.asSequence().map { it.copy(dictionaryId = id) }.map { it.toDbCard() }.toList()
        logger.info("Dictionary '${dictionary.name}': id = $id, cards = ${cards.size}")
        this.repositories.cardRepository.createCards(cards)
    }
}

internal fun loadBuiltinDocuments(language: String): Sequence<DocumentEntity> {
    val resources = RESOURCE_DOCUMENTS_BY_LANGUAGE[language] ?: RESOURCE_DOCUMENTS_BY_LANGUAGE[DEFAULT_TARGET_LANGUAGE]
    ?: return emptySequence()
    return resources.asSequence().map {
        checkNotNull(object {}.javaClass.getResourceAsStream(it)) { "Can't find resource $it" }
            .bufferedReader(Charsets.UTF_8)
            .readText()
    }.map {
        documentEntityFromJson(it)
    }
}
