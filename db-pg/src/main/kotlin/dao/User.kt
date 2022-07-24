package com.gitlab.sszuev.flashcards.dbpg.dao

import org.jetbrains.exposed.dao.id.LongIdTable

/**
 * id;login;pwd;role
 */
object User : LongIdTable("users") {
    val login = varchar("login", 255)
    val password = varchar("pwd", 255)
    val role = integer("role")
}