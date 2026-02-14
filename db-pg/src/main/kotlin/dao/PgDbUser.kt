package com.gitlab.sszuev.flashcards.dbpg.dao

import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.Entity
import org.jetbrains.exposed.v1.dao.EntityClass
import java.time.LocalDateTime

class PgDbUser(id: EntityID<String>) : Entity<String>(id) {
    companion object : EntityClass<String, PgDbUser>(Users)

    var createdAt: LocalDateTime by Users.createdAt
    var details: String by Users.details
}