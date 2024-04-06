package com.gitlab.sszuev.flashcards.dbmem.dao

import java.time.LocalDateTime

/**
 * id,name,user_id,source_lang,target_lang,details,changed_at
 */
data class MemDbDictionary(
    val name: String,
    val sourceLanguage: MemDbLanguage,
    val targetLanguage: MemDbLanguage,
    val details: Map<String, String> = emptyMap(),
    val userId: String? = null,
    val id: Long? = null,
    val changedAt: LocalDateTime? = null,
)