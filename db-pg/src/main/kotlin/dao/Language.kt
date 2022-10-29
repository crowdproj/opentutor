package com.gitlab.sszuev.flashcards.dbpg.dao

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID

class Language(id: EntityID<String>): Entity<String>(id) {
    companion object : EntityClass<String, Language>(Languages)
    var partsOfSpeech: String by Languages.partsOfSpeech
}