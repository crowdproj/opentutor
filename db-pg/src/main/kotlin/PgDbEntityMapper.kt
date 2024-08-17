package com.gitlab.sszuev.flashcards.dbpg

import com.gitlab.sszuev.flashcards.asKotlin
import com.gitlab.sszuev.flashcards.common.detailsAsCommonCardDetailsDto
import com.gitlab.sszuev.flashcards.common.parseCardDetailsJson
import com.gitlab.sszuev.flashcards.common.parseCardWordsJson
import com.gitlab.sszuev.flashcards.common.parseDictionaryDetailsJson
import com.gitlab.sszuev.flashcards.common.toCardEntityDetails
import com.gitlab.sszuev.flashcards.common.toCardEntityStats
import com.gitlab.sszuev.flashcards.common.toCardWordEntity
import com.gitlab.sszuev.flashcards.common.toJsonString
import com.gitlab.sszuev.flashcards.common.wordsAsCommonWordDtoList
import com.gitlab.sszuev.flashcards.dbpg.dao.Cards
import com.gitlab.sszuev.flashcards.dbpg.dao.Dictionaries
import com.gitlab.sszuev.flashcards.dbpg.dao.PgDbCard
import com.gitlab.sszuev.flashcards.dbpg.dao.PgDbDictionary
import com.gitlab.sszuev.flashcards.repositories.DbCard
import com.gitlab.sszuev.flashcards.repositories.DbDictionary
import com.gitlab.sszuev.flashcards.repositories.DbLang
import com.gitlab.sszuev.flashcards.repositories.LanguageRepository
import org.jetbrains.exposed.dao.id.EntityID
import java.time.LocalDateTime

internal fun PgDbDictionary.toDbDictionary(): DbDictionary = DbDictionary(
    dictionaryId = this.id.value.toString(),
    userId = this.userId,
    name = this.name,
    sourceLang = createDbLang(this.sourceLang),
    targetLang = createDbLang(this.targetLang),
    details = parseDictionaryDetailsJson(this.details)
)

internal fun PgDbCard.toCardEntity(): DbCard {
    val words = parseCardWordsJson(this.words)
    val details = parseCardDetailsJson(this.details)
    return DbCard(
        cardId = this.id.value.toString(),
        dictionaryId = this.dictionaryId.value.toString(),
        words = words.map { it.toCardWordEntity() },
        details = details.toCardEntityDetails(),
        stats = details.toCardEntityStats(),
        answered = this.answered,
        changedAt = this.changedAt.asKotlin(),
    )
}

internal fun writeCardEntityToPgDbCard(from: DbCard, to: PgDbCard, timestamp: LocalDateTime) {
    to.dictionaryId = from.dictionaryId.toDictionariesId()
    to.words = from.toPgDbCardWordsJson()
    to.answered = from.answered
    to.details = from.detailsAsCommonCardDetailsDto().toJsonString()
    to.changedAt = timestamp
}

internal fun DbCard.toPgDbCardWordsJson(): String = wordsAsCommonWordDtoList().toJsonString()

internal fun String.toDictionariesId(): EntityID<Long> = EntityID(toLong(), Dictionaries)

internal fun String.toCardsId(): EntityID<Long> = EntityID(toLong(), Cards)

internal fun createDbLang(tag: String) = DbLang(
    langId = tag,
    partsOfSpeech = LanguageRepository.partsOfSpeech(tag)
)