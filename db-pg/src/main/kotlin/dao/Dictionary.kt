package com.gitlab.sszuev.flashcards.dbpg.dao

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID

class Dictionary(id: EntityID<Long>) : Entity<Long>(id) {
    companion object : EntityClass<Long, Dictionary>(Dictionaries)

    var userId by Dictionaries.userId
    var name by Dictionaries.name
    val sourceLang by Language referencedOn Dictionaries.sourceLanguage
    val targetLand by Language referencedOn Dictionaries.targetLanguage
}