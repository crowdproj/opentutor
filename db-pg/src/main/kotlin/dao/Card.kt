package com.gitlab.sszuev.flashcards.dbpg.dao

import org.jetbrains.exposed.dao.id.LongIdTable

/**
 * id;dictionary_id;text;transcription;part_of_speech;details;answered
 */
object Card : LongIdTable("cards") {
    val dictionary = reference("dictionary_id", Dictionary.id).index()
    val text = text("text")
    val transcription = text("transcription")
    val partOfSpeech = varchar("part_of_speech", 255)
    val details = text("details")
    val answered = integer("answered")
}