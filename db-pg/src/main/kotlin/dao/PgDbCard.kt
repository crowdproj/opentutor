package com.gitlab.sszuev.flashcards.dbpg.dao

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID

class PgDbCard(id: EntityID<Long>) : Entity<Long>(id) {
    companion object : EntityClass<Long, PgDbCard>(Cards)

    var dictionaryId by Cards.dictionaryId
    var words by Cards.words
    var answered by Cards.answered
    var details by Cards.details
    var changedAt by Cards.changedAt
}