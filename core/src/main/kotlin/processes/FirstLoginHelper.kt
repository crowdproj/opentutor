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

private const val DEFAULT_TARGET_LANGUAGE = "ru"
private val RESOURCE_DOCUMENTS_BY_LANGUAGE = mapOf(
    "ru" to listOf(
        "/irregular-verbs-en-ru.json",
        "/weather-en-ru.json",
        "/common-words-01-en-ru.json",
        "/common-words-02-en-ru.json",
        "/common-words-03-en-ru.json",
        "/common-words-04-en-ru.json",
        "/common-words-05-en-ru.json",
        "/common-words-06-en-ru.json",
        "/common-words-07-en-ru.json",
        "/common-words-08-en-ru.json",
        "/common-words-08-en-ru.json",
        "/common-words-10-en-ru.json",
        "/common-words-11-en-ru.json",
        "/blindsight_by_peter_watts.json"
    ),
    "en" to listOf("/weather-zh-en.json"),
    "zh" to listOf("/irregular-verbs-en-zh.json", "/weather-en-zh.json"),
    "es" to listOf("/irregular-verbs-en-es.json", "/weather-en-es.json"),
    "pt" to listOf("/irregular-verbs-en-pt.json", "/weather-en-pt.json"),
    "ja" to listOf("/irregular-verbs-en-ja.json", "/weather-en-ja.json"),
    "de" to listOf("/irregular-verbs-en-de.json", "/weather-en-de.json"),
    "fr" to listOf("/irregular-verbs-en-fr.json", "/weather-en-fr.json"),
    "it" to listOf("/irregular-verbs-en-it.json", "/weather-en-it.json"),
    "pl" to listOf("/irregular-verbs-en-pl.json", "/weather-en-pl.json"),
    "nl" to listOf("/irregular-verbs-en-nl.json", "/weather-en-nl.json"),
    "el" to listOf("/irregular-verbs-en-el.json", "/weather-en-el.json"),
    "hu" to listOf("/irregular-verbs-en-hu.json", "/weather-en-hu.json"),
    "cs" to listOf("/irregular-verbs-en-cs.json", "/weather-en-cs.json"),
    "sv" to listOf("/irregular-verbs-en-sv.json", "/weather-en-sv.json"),
    "bg" to listOf("/irregular-verbs-en-bg.json", "/weather-en-bg.json"),
    "da" to listOf("/irregular-verbs-en-da.json", "/weather-en-da.json"),
    "fi" to listOf("/irregular-verbs-en-fi.json", "/weather-en-fi.json"),
    "sk" to listOf("/irregular-verbs-en-sk.json", "/weather-en-sk.json"),
    "lt" to listOf("/irregular-verbs-en-lt.json", "/weather-en-lt.json"),
    "lv" to listOf("/irregular-verbs-en-lv.json", "/weather-en-lv.json"),
    "sl" to listOf("/irregular-verbs-en-sl.json", "/weather-en-sl.json"),
    "et" to listOf("/irregular-verbs-en-et.json", "/weather-en-et.json"),
    "mt" to listOf("/irregular-verbs-en-mt.json", "/weather-en-mt.json"),
    "hi" to listOf("/irregular-verbs-en-hi.json", "/weather-en-hi.json"),
    "ar" to listOf("/irregular-verbs-en-ar.json", "/weather-en-ar.json"),
    "bn" to listOf("/irregular-verbs-en-bn.json", "/weather-en-bn.json"),
    "pa" to listOf("/irregular-verbs-en-pa.json", "/weather-en-pa.json"),
    "vi" to listOf("/irregular-verbs-en-vi.json", "/weather-en-vi.json"),
    "mr" to listOf("/irregular-verbs-en-mr.json", "/weather-en-mr.json"),
    "te" to listOf("/irregular-verbs-en-te.json", "/weather-en-te.json"),
    "jv" to listOf("/irregular-verbs-en-jv.json", "/weather-en-jv.json"),
    "ko" to listOf("/irregular-verbs-en-ko.json", "/weather-en-ko.json"),
    "ta" to listOf("/irregular-verbs-en-ta.json", "/weather-en-ta.json"),
    "tr" to listOf("/irregular-verbs-en-tr.json", "/weather-en-tr.json"),
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
