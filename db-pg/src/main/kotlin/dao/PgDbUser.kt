package com.gitlab.sszuev.flashcards.dbpg.dao

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.time.LocalDateTime

class PgDbUser(id: EntityID<String>) : Entity<String>(id) {
    companion object : EntityClass<String, PgDbUser>(Users)

    var createdAt: LocalDateTime by Users.createdAt
    var details: String by Users.details
}