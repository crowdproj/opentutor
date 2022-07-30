package com.gitlab.sszuev.flashcards.dbpg.dao

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID

class Card(id: EntityID<Long>) : Entity<Long>(id) {
    companion object : EntityClass<Long, Card>(Cards)

    var dictionaryId by Cards.dictionaryId
    var text by Cards.text
    var transcription by Cards.transcription
    var partOfSpeech by Cards.partOfSpeech
    var details by Cards.details
    var answered by Cards.answered
    val examples by Example referrersOn Examples.card
    val translations by Translation referrersOn Translations.card
}