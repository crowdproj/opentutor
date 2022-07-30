package com.gitlab.sszuev.flashcards.dbpg.dao

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Column

/**
 * id;name;user_id;source_lang;target_lang
 */
object Dictionaries : LongIdTable("dictionaries") {
    val name = varchar("name", 256)
    val user = reference("user_id", Users.id).index()
    val sourceLanguage = reference("source_lang", Languages.id)
    val targetLanguage = reference("target_lang", Languages.id)
}

/**
 * id;dictionary_id;text;transcription;part_of_speech;details;answered
 */
object Cards : LongIdTable("cards") {
    val dictionaryId = reference("dictionary_id", id).index()
    val text = text("text")
    val transcription = text("transcription").nullable()
    val partOfSpeech = varchar("part_of_speech", 255).nullable()
    val details = text("details").nullable()
    val answered = integer("answered").nullable()
}

/**
 * id;card_id;text
 */
object Examples : LongIdTable("examples") {
    val card = reference("card_id", Cards.id).index()
    val text = text("text")
}

/**
 * id;card_id;text
 */
object Translations : LongIdTable("translations") {
    val card = reference("card_id", Cards.id).index()
    val text = text("text")
}

/**
 * id;login;pwd;role
 */
object Users : LongIdTable("users") {
    val login = varchar("login", 255)
    val password = varchar("pwd", 255)
    val role = integer("role")
}

/**
 * id;parts_of_speech
 */
object Languages : StringIdTable("languages") {
    val partsOfSpeech = varchar("parts_of_speech", 255)
}

open class StringIdTable(name: String = "", columnName: String = "id", columnLength: Int = 50) : IdTable<String>(name) {
    override val id: Column<EntityID<String>> = varchar(columnName, columnLength).uniqueIndex().entityId()
    override val primaryKey by lazy { super.primaryKey ?: PrimaryKey(id) }
}