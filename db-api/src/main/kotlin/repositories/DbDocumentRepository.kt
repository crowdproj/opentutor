package com.gitlab.sszuev.flashcards.repositories

/**
 * To work both with [DbDictionary] & [DbCard]
 */
interface DbDocumentRepository {

    /**
     * Saves a document (= dictionary with its cards).
     * @param dictionary [DbDictionary]
     * @param cards List<[DbCard]>
     * @return dictionary ID
     */
    fun save(dictionary: DbDictionary, cards: List<DbCard>): String
}