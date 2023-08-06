package com.gitlab.sszuev.flashcards.dbpg

import com.gitlab.sszuev.flashcards.common.LanguageRepository
import com.gitlab.sszuev.flashcards.common.asKotlin
import com.gitlab.sszuev.flashcards.common.asLong
import com.gitlab.sszuev.flashcards.common.detailsAsCommonCardDetailsDto
import com.gitlab.sszuev.flashcards.common.documents.DocumentCard
import com.gitlab.sszuev.flashcards.common.parseCardDetailsJson
import com.gitlab.sszuev.flashcards.common.parseCardWordsJson
import com.gitlab.sszuev.flashcards.common.toCardEntityDetails
import com.gitlab.sszuev.flashcards.common.toCardEntityStats
import com.gitlab.sszuev.flashcards.common.toCardWordEntity
import com.gitlab.sszuev.flashcards.common.toCommonCardDtoDetails
import com.gitlab.sszuev.flashcards.common.toCommonWordDtoList
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
import com.gitlab.sszuev.flashcards.model.domain.CardEntity
import com.gitlab.sszuev.flashcards.model.domain.CardId
import com.gitlab.sszuev.flashcards.model.domain.DictionaryEntity
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
import com.gitlab.sszuev.flashcards.model.domain.LangEntity
import com.gitlab.sszuev.flashcards.model.domain.LangId
import com.gitlab.sszuev.flashcards.model.domain.Stage
import org.jetbrains.exposed.dao.id.EntityID
import java.time.LocalDateTime
import java.util.UUID

internal fun PgDbUser.toAppUserEntity(): AppUserEntity = AppUserEntity(
    id = this.id.asUserId(),
    authId = this.uuid.asAppAuthId(),
)

internal fun PgDbDictionary.toDictionaryEntity(): DictionaryEntity = DictionaryEntity(
    dictionaryId = this.id.asDictionaryId(),
    name = this.name,
    sourceLang = createLangEntity(this.sourceLang),
    targetLang = createLangEntity(this.targetLang),
)

internal fun PgDbCard.toCardEntity(): CardEntity {
    val words = parseCardWordsJson(this.words)
    val details = parseCardDetailsJson(this.details)
    return CardEntity(
        cardId = this.id.asCardId(),
        dictionaryId = this.dictionaryId.asDictionaryId(),
        words = words.map { it.toCardWordEntity() },
        details = details.toCardEntityDetails(),
        stats = details.toCardEntityStats(),
        answered = this.answered,
        changedAt = this.changedAt.asKotlin(),
    )
}

internal fun writeCardEntityToPgDbCard(from: CardEntity, to: PgDbCard, timestamp: LocalDateTime) {
    to.dictionaryId = from.dictionaryId.asRecordId()
    to.words = from.toPgDbCardWordsJson()
    to.answered = from.answered
    to.details = from.detailsAsCommonCardDetailsDto().toJsonString()
    to.changedAt = timestamp
}

internal fun Map<Stage, Long>.toPgDbCardDetailsJson(): String = toCommonCardDtoDetails().toJsonString()

internal fun CardEntity.toPgDbCardWordsJson(): String = wordsAsCommonWordDtoList().toJsonString()

internal fun DocumentCard.toPgDbCardWordsJson(): String = toCommonWordDtoList().toJsonString()

internal fun EntityID<Long>.asUserId(): AppUserId = AppUserId(value.toString())

internal fun EntityID<Long>.asDictionaryId(): DictionaryId = value.asDictionaryId()

internal fun EntityID<Long>.asCardId(): CardId = value.asCardId()

internal fun AppUserId.asRecordId(): EntityID<Long> = EntityID(asLong(), Users)

internal fun DictionaryId.asRecordId(): EntityID<Long> = EntityID(asLong(), Dictionaries)

internal fun CardId.asRecordId(): EntityID<Long> = EntityID(asLong(), Cards)

internal fun createLangEntity(tag: String) = LangEntity(
    langId = LangId(tag),
    partsOfSpeech = LanguageRepository.partsOfSpeech(tag)
)

private fun UUID.asAppAuthId(): AppAuthId = AppAuthId(toString())

internal fun Long.asDictionaryId(): DictionaryId = DictionaryId(toString())

internal fun Long.asCardId(): CardId = CardId(toString())