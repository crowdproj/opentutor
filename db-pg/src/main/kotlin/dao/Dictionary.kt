package com.gitlab.sszuev.flashcards.dbpg.dao

import org.jetbrains.exposed.dao.id.LongIdTable

/**
 * id;name;user_id;source_lang;target_lang
 */
object Dictionary : LongIdTable("dictionaries") {
    val name = varchar("name", 256)
    val user = reference("user_id", User.id).index()
    val sourceLanguage = reference("source_lang", Language.id)
    val targetLanguage = reference("target_lang", Language.id)
}