package com.gitlab.sszuev.flashcards.dbpg.dao

/**
 * id;parts_of_speech
 */
object Language : StringIdTable("languages") {
    val partsOfSpeech = varchar("parts_of_speech", 255)
}