package com.gitlab.sszuev.flashcards.dbpg.dao

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID

class Example(id: EntityID<Long>): Entity<Long>(id) {
    companion object : EntityClass<Long, Example>(Examples)
    var text by Examples.text
}