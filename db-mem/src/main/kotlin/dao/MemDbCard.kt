package com.gitlab.sszuev.flashcards.dbmem.dao

import java.time.LocalDateTime

/**
 * id,dictionary_id,text,word,examples,details,answered,changed_at
 */
data class MemDbCard(
    val text: String,
    val words: List<MemDbWord>,
    val details: Map<String, String> = emptyMap(),
    val id: Long? = null,
    val dictionaryId: Long? = null,
    val answered: Int? = null,
    val changedAt: LocalDateTime? = null,
)

