package com.gitlab.sszuev.flashcards.dbpg.dao

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column

open class StringIdTable(name: String = "", columnName: String = "id", columnLength: Int = 50) : IdTable<String>(name) {
    override val id: Column<EntityID<String>> = varchar(columnName, columnLength).uniqueIndex().entityId()
    override val primaryKey by lazy { super.primaryKey ?: PrimaryKey(id) }
}