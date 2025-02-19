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

private val RESOURCE_DOCUMENTS_BY_LOCALE = mapOf(
    "ru" to listOf("/irregular-verbs-en-ru.json", "/weather-en-ru.json"),
    "zh" to listOf("/weather-en-zh.json"),
    "es" to listOf("/weather-en-es.json"),
)

internal val users = Caffeine.newBuilder().maximumSize(1024).build<AppAuthId, DbUser>()

internal fun DbUserRepository.createOrUpdateUser(id: AppAuthId, locale: String, onCreateOrUpdate: () -> Unit) {
    var dbUser = users.getIfPresent(id)
    if (dbUser != null && dbUser.details.containsKey("locale")) {
        return
    }
    if (dbUser == null) {
        dbUser = findByUserId(id.asString())
    }
    if (dbUser == null) {
        dbUser = DbUser(id = id.asString(), details = mapOf("locale" to locale))
        users.put(id, this.createUser(dbUser))
        onCreateOrUpdate()
        return
    }
    if (dbUser.details.containsKey("locale")) {
        users.put(id, dbUser)
        return
    }
    dbUser = dbUser.copy(details = dbUser.details + mapOf("locale" to locale))
    this.updateUser(dbUser)
    users.put(id, dbUser)
    onCreateOrUpdate()
    return
}

internal fun DbUserRepository.getOrCreateUser(id: AppAuthId): DbUser {
    var dbUser = users.getIfPresent(id)
    if (dbUser != null) {
        return dbUser
    }
    dbUser = findByUserId(id.asString())
    if (dbUser == null) {
        dbUser = DbUser(id = id.asString())
        users.put(id, this.createUser(dbUser))
        return dbUser
    }
    users.put(id, dbUser)
    return dbUser
}

internal fun DbUserRepository.putUser(id: AppAuthId ,dbUser: DbUser) {
    users.put(id, this.updateUser(dbUser))
}

internal fun DictionaryContext.populateBuiltinDictionaries(locale: String) {
    val userId = this.normalizedRequestAppAuthId
    val documents = loadBuiltinDocuments(locale).toList()

    documents.forEach { document ->
        val dictionary = document.dictionary.copy(userId = userId).toDbDictionary()
        logger.info("Create dictionary '${dictionary.name}'")
        val id = DictionaryId(this.repositories.dictionaryRepository.createDictionary(dictionary).dictionaryId)
        val cards = document.cards.asSequence().map { it.copy(dictionaryId = id) }.map { it.toDbCard() }.toList()
        logger.info("Dictionary '${dictionary.name}': id = $id, cards = ${cards.size}")
        this.repositories.cardRepository.createCards(cards)
    }
}

internal fun loadBuiltinDocuments(locale: String): Sequence<DocumentEntity> {
    val resources = RESOURCE_DOCUMENTS_BY_LOCALE[locale] ?: return emptySequence()
    return resources.asSequence().map {
        checkNotNull(object {}.javaClass.getResourceAsStream(it)) { "Can't find resource $it" }
            .bufferedReader(Charsets.UTF_8)
            .readText()
    }.map {
        documentEntityFromJson(it)
    }
}
