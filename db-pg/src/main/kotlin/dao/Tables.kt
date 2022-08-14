package com.gitlab.sszuev.flashcards.dbpg.dao

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column

/**
 * id;name;user_id;source_lang;target_lang
 */
object Dictionaries : LongIdTableWithSequence(
    tableName = "dictionaries",
    idSeqName = "dictionaries_id_seq",
    pkeyName = "dictionaries_pkey"
) {
    val name = varchar("name", 256)
    val userId = reference("user_id", Users.id).index()
    val sourceLanguage = reference("source_lang", Languages.id)
    val targetLanguage = reference("target_lang", Languages.id)
}

/**
 * id;dictionary_id;text;transcription;part_of_speech;details;answered
 */
object Cards : LongIdTableWithSequence(tableName = "cards", idSeqName = "cards_id_seq", pkeyName = "cards_pkey") {
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
object Examples :
    LongIdTableWithSequence(tableName = "examples", idSeqName = "examples_id_seq", pkeyName = "examples_pkey") {
    val cardId = reference("card_id", Cards.id).index()
    val text = text("text")
}

/**
 * id;card_id;text
 */
object Translations : LongIdTableWithSequence(
    tableName = "translations",
    idSeqName = "translations_id_seq",
    pkeyName = "translations_pkey"
) {
    val cardId = reference("card_id", Cards.id).index()
    val text = text("text")
}

/**
 * id;login;pwd;role
 */
object Users : LongIdTableWithSequence(tableName = "users", idSeqName = "users_id_seq", pkeyName = "users_pkey") {
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

open class LongIdTableWithSequence(tableName: String, idSeqName: String, pkeyName: String, columnName: String = "id") :
    IdTable<Long>(tableName) {
    final override val id: Column<EntityID<Long>> = long(columnName).autoIncrement(idSeqName).entityId()
    override val primaryKey = PrimaryKey(id, name = pkeyName)
}