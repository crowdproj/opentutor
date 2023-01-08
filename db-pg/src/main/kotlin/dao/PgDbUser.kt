package com.gitlab.sszuev.flashcards.dbpg.dao

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID

class PgDbUser(id: EntityID<Long>) : Entity<Long>(id) {
    companion object : EntityClass<Long, PgDbUser>(Users)

    var uuid by Users.uuid
    var details by Users.details
    var changedAt by Users.changedAt
}