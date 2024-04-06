package com.gitlab.sszuev.flashcards.dbpg.dao

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import org.postgresql.util.PGobject
import java.sql.ResultSet
import java.time.LocalDateTime


/**
 * id;name;user_id;source_lang;target_lang
 */
object Dictionaries : LongIdTableWithSequence(
    tableName = "dictionaries",
    idSeqName = "dictionaries_id_seq",
    pkeyName = "dictionaries_pkey"
) {
    val name: Column<String> = varchar("name", 1024)
    val userId: Column<String> = varchar("user_id", 36)
    val sourceLanguage: Column<String> = varchar("source_lang", 42)
    val targetLanguage: Column<String> = varchar("target_lang", 42)
    val details: Column<String> = json("details")
    val changedAt: Column<LocalDateTime> = datetime("changed_at")
}

/**
 * id,dictionary_id,text,words,details,answered,changed_at
 */
object Cards : LongIdTableWithSequence(tableName = "cards", idSeqName = "cards_id_seq", pkeyName = "cards_pkey") {
    val dictionaryId = reference("dictionary_id", id).index()
    val words = jsonb("words")
    val answered = integer("answered").nullable()
    val details = json("details")
    val changedAt = datetime("changed_at")
}

open class LongIdTableWithSequence(tableName: String, idSeqName: String, pkeyName: String, columnName: String = "id") :
    IdTable<Long>(tableName) {
    final override val id: Column<EntityID<Long>> = long(columnName).autoIncrement(idSeqName).entityId()
    override val primaryKey = PrimaryKey(id, name = pkeyName)
}

fun Table.jsonb(name: String): Column<String> = registerColumn(name, JsonColumnType("jsonb"))

fun Table.json(name: String): Column<String> = registerColumn(name, JsonColumnType("json"))

class JsonColumnType(private val sqlType: String) : ColumnType() {
    override fun sqlType(): String = sqlType

    override fun notNullValueToDB(value: Any): Any {
        val res = PGobject()
        res.type = sqlType
        res.value = value.toString()
        return res
    }

    override fun nonNullValueToString(value: Any): String {
        return value.toString().substringAfter("'").substringBefore("'::$sqlType")
    }

    override fun readObject(rs: ResultSet, index: Int): Any? = rs.getString(index)
}