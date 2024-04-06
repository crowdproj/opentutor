package com.gitlab.sszuev.flashcards.dbpg.dao

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.time.LocalDateTime

class PgDbDictionary(id: EntityID<Long>) : Entity<Long>(id) {
    companion object : EntityClass<Long, PgDbDictionary>(Dictionaries)

    var userId: String by Dictionaries.userId
    var name: String by Dictionaries.name
    val sourceLang: String by Dictionaries.sourceLanguage
    val targetLang: String by Dictionaries.targetLanguage
    var details: String by Dictionaries.details
    var changedAt: LocalDateTime by Dictionaries.changedAt
}