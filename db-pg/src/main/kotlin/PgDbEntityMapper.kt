package com.gitlab.sszuev.flashcards.dbpg

import com.gitlab.sszuev.flashcards.common.LanguageRepository
import com.gitlab.sszuev.flashcards.common.asLong
import com.gitlab.sszuev.flashcards.common.documents.DocumentCard
import com.gitlab.sszuev.flashcards.common.parseCardDetailsJson
import com.gitlab.sszuev.flashcards.common.parseCardWordsJson
import com.gitlab.sszuev.flashcards.common.toCommonCardDtoDetails
import com.gitlab.sszuev.flashcards.common.toCommonWordDtoList
import com.gitlab.sszuev.flashcards.common.toJsonString
import com.gitlab.sszuev.flashcards.dbpg.dao.Cards
import com.gitlab.sszuev.flashcards.dbpg.dao.DbPgCard
import com.gitlab.sszuev.flashcards.dbpg.dao.Dictionaries
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

internal fun DbPgCard.toCardEntity(): CardEntity {
    val word = parseCardWordsJson(this.words).first()
    val details =
        parseCardDetailsJson(this.details).mapKeys { Stage.valueOf(it.key) }.mapValues { it.value.toString().toLong() }
    return CardEntity(
        cardId = this.id.asCardId(),
        dictionaryId = this.dictionaryId.asDictionaryId(),
        word = this.text,
        transcription = word.transcription,
        details = details,
        partOfSpeech = word.partOfSpeech,
        answered = this.answered,
        examples = word.examples.map { it.text },
        translations = word.translations,
    )
}

internal fun writeCardEntityToPgDbCard(from: CardEntity, to: DbPgCard, timestamp: LocalDateTime) {
    to.dictionaryId = from.dictionaryId.asRecordId()
    to.text = from.word
    to.words = from.toPgDbCardWordsJson()
    to.answered = from.answered
    to.details = from.details.toPgDbCardDetailsJson()
    to.changedAt = timestamp
}

internal fun Map<Stage, Long>.toPgDbCardDetailsJson(): String = toCommonCardDtoDetails().toJsonString()

internal fun CardEntity.toPgDbCardWordsJson(): String = toCommonWordDtoList().toJsonString()

internal fun DocumentCard.toPgDbCardWordsJson(): String = toCommonWordDtoList().toJsonString()

internal fun EntityID<Long>.asUserId(): AppUserId = AppUserId(value.toString())

internal fun EntityID<Long>.asDictionaryId(): DictionaryId = DictionaryId(value.toString())

internal fun EntityID<Long>.asCardId(): CardId = CardId(value.toString())

internal fun AppUserId.asRecordId(): EntityID<Long> = EntityID(asLong(), Users)

internal fun DictionaryId.asRecordId(): EntityID<Long> = EntityID(asLong(), Dictionaries)

internal fun CardId.asRecordId(): EntityID<Long> = EntityID(asLong(), Cards)

internal fun createLangEntity(tag: String) = LangEntity(
    langId = LangId(tag),
    partsOfSpeech = LanguageRepository.partsOfSpeech(tag)
)

private fun UUID.asAppAuthId(): AppAuthId = AppAuthId(toString())