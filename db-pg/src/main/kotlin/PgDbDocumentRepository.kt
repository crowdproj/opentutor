package com.gitlab.sszuev.flashcards.dbpg

import com.gitlab.sszuev.flashcards.common.detailsAsCommonCardDetailsDto
import com.gitlab.sszuev.flashcards.common.toJsonString
import com.gitlab.sszuev.flashcards.dbpg.dao.Cards
import com.gitlab.sszuev.flashcards.dbpg.dao.Dictionaries
import com.gitlab.sszuev.flashcards.repositories.DbCard
import com.gitlab.sszuev.flashcards.repositories.DbDictionary
import com.gitlab.sszuev.flashcards.repositories.DbDocumentRepository
import com.gitlab.sszuev.flashcards.systemNow
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.batchInsert
import org.jetbrains.exposed.v1.jdbc.insertAndGetId

class PgDbDocumentRepository(
    dbConfig: PgDbConfig = PgDbConfig.DEFAULT,
) : DbDocumentRepository {

    private val connection: Database by lazy {
        // lazy, to avoid initialization error when there is no real pg-database
        // and memory-storage is used instead
        PgDbStandardConnector.connector(dbConfig).database
    }

    // enforce connection
    fun connect() {
        connection
    }

    override fun save(
        dictionary: DbDictionary,
        cards: List<DbCard>
    ) = connection.execute {
        require(dictionary != DbDictionary.NULL)
        require(cards.all { it != DbCard.NULL })
        if (dictionary.dictionaryId.isNotBlank()) {
            throw IllegalArgumentException("The specified dictionary has id = ${dictionary.dictionaryId}")
        }
        require(dictionary.userId.isNotBlank()) { "The specified dictionary has no user-id" }
        if (!cards.all { it.dictionaryId.isBlank() && it.cardId.isBlank() }) {
            throw IllegalArgumentException("Some or all of the specified cards have id or dictionaryId")
        }

        val timestamp = systemNow()
        val dictionaryId = Dictionaries.insertAndGetId {
            it[sourceLanguage] = dictionary.sourceLang.langId
            it[targetLanguage] = dictionary.targetLang.langId
            it[name] = dictionary.name
            it[userId] = dictionary.userId
            it[changedAt] = timestamp
            it[details] = dictionary.detailsAsCommonCardDetailsDto().toJsonString()
        }
        Cards.batchInsert(cards) { card ->
            this[Cards.dictionaryId] = dictionaryId
            this[Cards.words] = card.toPgDbCardWordsJson()
            this[Cards.answered] = card.answered
            this[Cards.details] = card.detailsAsCommonCardDetailsDto().toJsonString()
            this[Cards.changedAt] = timestamp
        }
        dictionaryId.value.toString()
    }
}