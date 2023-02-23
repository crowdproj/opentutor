package com.gitlab.sszuev.flashcards.dbpg.dao

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID

class PgDbDictionary(id: EntityID<Long>) : Entity<Long>(id) {
    companion object : EntityClass<Long, PgDbDictionary>(Dictionaries)

    var userId by Dictionaries.userId
    var name by Dictionaries.name
    val sourceLang by Dictionaries.sourceLanguage
    val targetLang by Dictionaries.targetLanguage
    var details by Dictionaries.details
    var changedAt by Dictionaries.changedAt
}