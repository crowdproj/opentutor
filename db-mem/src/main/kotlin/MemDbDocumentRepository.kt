package com.gitlab.sszuev.flashcards.dbmem

import com.gitlab.sszuev.flashcards.repositories.DbCard
import com.gitlab.sszuev.flashcards.repositories.DbDictionary
import com.gitlab.sszuev.flashcards.repositories.DbDocumentRepository
import com.gitlab.sszuev.flashcards.systemNow
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger(MemDbDocumentRepository::class.java)

class MemDbDocumentRepository(
    dbConfig: MemDbConfig = MemDbConfig(),
) : DbDocumentRepository {
    private val database by lazy { MemDatabase.get(dbConfig.dataLocation) }

    override fun save(
        dictionary: DbDictionary,
        cards: List<DbCard>
    ): String {
        require(dictionary != DbDictionary.NULL)
        require(cards.all { it != DbCard.NULL })
        if (dictionary.dictionaryId.isNotBlank()) {
            throw IllegalArgumentException("The specified dictionary has id = ${dictionary.dictionaryId}")
        }
        require(dictionary.userId.isNotBlank()) { "The specified dictionary has no user-id" }
        if (!cards.all { it.dictionaryId.isBlank() && it.cardId.isBlank() }) {
            throw IllegalArgumentException("Some or all of the specified cards have id or dictionaryId")
        }
        val changedAt = systemNow()
        val dictionaryId =
            checkNotNull(database.saveDictionary(dictionary.toMemDbDictionary().copy(changedAt = changedAt)).id)
        val cardIds = mutableSetOf<Long>()
        try {
            cards.forEach { card ->
                database.saveCard(
                    card.toMemDbCard().copy(id = null, changedAt = changedAt, dictionaryId = dictionaryId)
                ).id?.let {
                    cardIds.add(it)
                }
            }
        } catch (ex: Exception) {
            logger.error("Error during saving cards for dictionary-id=$dictionaryId", ex)
            cardIds.forEach {
                database.deleteCardById(it)
            }
            database.deleteDictionaryById(dictionaryId)
            throw ex
        }
        return dictionaryId.toString()
    }
}