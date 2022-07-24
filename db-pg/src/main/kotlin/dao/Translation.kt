package com.gitlab.sszuev.flashcards.dbpg.dao

import org.jetbrains.exposed.dao.id.LongIdTable

/**
 * id;card_id;text
 */
object Translation : LongIdTable("translations") {
    val card = reference("card_id", id).index()
    val text = text("text")
}