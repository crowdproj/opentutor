package com.gitlab.sszuev.flashcards.dbpg.dao

import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.Entity
import org.jetbrains.exposed.v1.dao.EntityClass

class PgDbCard(id: EntityID<Long>) : Entity<Long>(id) {
    companion object : EntityClass<Long, PgDbCard>(Cards)

    var dictionaryId by Cards.dictionaryId
    var words by Cards.words
    var answered by Cards.answered
    var details by Cards.details
    var changedAt by Cards.changedAt
}