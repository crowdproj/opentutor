package com.gitlab.sszuev.flashcards.dbmem.dao

import java.time.LocalDateTime

/**
 * id,dictionary_id,words,details,answered,changed_at
 */
data class MemDbCard(
    val id: Long? = null,
    val dictionaryId: Long? = null,
    val words: List<MemDbWord>,
    val details: Map<String, Any> = emptyMap(),
    val answered: Int? = null,
    val changedAt: LocalDateTime? = null,
)

