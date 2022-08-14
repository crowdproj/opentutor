package com.gitlab.sszuev.flashcards.dbpg.dao

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID

class Translation(id: EntityID<Long>) : Entity<Long>(id) {
    companion object : EntityClass<Long, Translation>(Translations)
    var cardId by Translations.cardId
    var text by Translations.text
}