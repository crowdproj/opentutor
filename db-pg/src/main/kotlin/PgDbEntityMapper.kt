package com.gitlab.sszuev.flashcards.dbpg

import com.gitlab.sszuev.flashcards.common.LanguageRepository
import com.gitlab.sszuev.flashcards.common.asKotlin
import com.gitlab.sszuev.flashcards.common.detailsAsCommonCardDetailsDto
import com.gitlab.sszuev.flashcards.common.parseCardDetailsJson
import com.gitlab.sszuev.flashcards.common.parseCardWordsJson
import com.gitlab.sszuev.flashcards.common.toCardEntityDetails
import com.gitlab.sszuev.flashcards.common.toCardEntityStats
import com.gitlab.sszuev.flashcards.common.toCardWordEntity
import com.gitlab.sszuev.flashcards.common.toJsonString
import com.gitlab.sszuev.flashcards.common.wordsAsCommonWordDtoList
import com.gitlab.sszuev.flashcards.dbpg.dao.Cards
import com.gitlab.sszuev.flashcards.dbpg.dao.Dictionaries
import com.gitlab.sszuev.flashcards.dbpg.dao.PgDbCard
import com.gitlab.sszuev.flashcards.dbpg.dao.PgDbDictionary
import com.gitlab.sszuev.flashcards.dbpg.dao.PgDbUser
import com.gitlab.sszuev.flashcards.dbpg.dao.Users
import com.gitlab.sszuev.flashcards.model.common.AppAuthId
import com.gitlab.sszuev.flashcards.model.common.AppUserEntity
import com.gitlab.sszuev.flashcards.model.common.AppUserId
import com.gitlab.sszuev.flashcards.repositories.DbCard
import com.gitlab.sszuev.flashcards.repositories.DbDictionary
import com.gitlab.sszuev.flashcards.repositories.DbLang
import org.jetbrains.exposed.dao.id.EntityID
import java.time.LocalDateTime
import java.util.UUID

internal fun PgDbUser.toAppUserEntity(): AppUserEntity = AppUserEntity(
    id = this.id.asUserId(),
    authId = this.uuid.asAppAuthId(),
)

internal fun PgDbDictionary.toDbDictionary(): DbDictionary = DbDictionary(
    dictionaryId = this.id.value.toString(),
    userId = this.userId.value.toString(),
    name = this.name,
    sourceLang = createDbLang(this.sourceLang),
    targetLang = createDbLang(this.targetLang),
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

internal fun EntityID<Long>.asUserId(): AppUserId = AppUserId(value.toString())

internal fun String.toDictionariesId(): EntityID<Long> = EntityID(toLong(), Dictionaries)

internal fun String.toCardsId(): EntityID<Long> = EntityID(toLong(), Cards)

internal fun String.toUserId(): EntityID<Long> = EntityID(toLong(), Users)

internal fun createDbLang(tag: String) = DbLang(
    langId = tag,
    partsOfSpeech = LanguageRepository.partsOfSpeech(tag)
)

private fun UUID.asAppAuthId(): AppAuthId = AppAuthId(toString())
