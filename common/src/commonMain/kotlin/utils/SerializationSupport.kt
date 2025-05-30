package com.gitlab.sszuev.flashcards.utils

import com.gitlab.sszuev.flashcards.CardContext
import com.gitlab.sszuev.flashcards.DictionaryContext
import com.gitlab.sszuev.flashcards.SettingsContext
import com.gitlab.sszuev.flashcards.TTSContext
import com.gitlab.sszuev.flashcards.TranslationContext
import com.gitlab.sszuev.flashcards.model.Id
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
import com.gitlab.sszuev.flashcards.model.domain.DocumentEntity
import com.gitlab.sszuev.flashcards.model.domain.TTSResourceId
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.Json.Default.decodeFromString
import kotlinx.serialization.modules.SerializersModule

private val module = SerializersModule {
    polymorphic(Any::class, String::class, String.serializer())
    polymorphic(Any::class, Int::class, Int.serializer())
    polymorphic(Any::class, Double::class, Double.serializer())
    polymorphic(Any::class, Boolean::class, Boolean.serializer())
    polymorphic(Id::class, Id.NONE::class, Id.NONE.serializer())
    polymorphic(Id::class, TTSResourceId::class, TTSResourceId.serializer())
    polymorphic(Id::class, DictionaryId::class, DictionaryId.serializer())
}

@Suppress("OPT_IN_USAGE")
private val cbor = Cbor {
    encodeDefaults = false
    ignoreUnknownKeys = true
    serializersModule = module
}

private val json = Json {
    encodeDefaults = false
    ignoreUnknownKeys = true
    prettyPrint = true
}

@OptIn(ExperimentalSerializationApi::class)
fun CardContext.toByteArray(): ByteArray = cbor.encodeToByteArray(CardContext.serializer(), this)

@OptIn(ExperimentalSerializationApi::class)
fun cardContextFromByteArray(bytes: ByteArray): CardContext = cbor.decodeFromByteArray(CardContext.serializer(), bytes)

@OptIn(ExperimentalSerializationApi::class)
fun DictionaryContext.toByteArray(): ByteArray = cbor.encodeToByteArray(DictionaryContext.serializer(), this)

@OptIn(ExperimentalSerializationApi::class)
fun dictionaryContextFromByteArray(bytes: ByteArray): DictionaryContext =
    cbor.decodeFromByteArray(DictionaryContext.serializer(), bytes)

@OptIn(ExperimentalSerializationApi::class)
fun TTSContext.toByteArray(): ByteArray = cbor.encodeToByteArray(TTSContext.serializer(), this)

@OptIn(ExperimentalSerializationApi::class)
fun ttsContextFromByteArray(bytes: ByteArray): TTSContext = cbor.decodeFromByteArray(TTSContext.serializer(), bytes)

@OptIn(ExperimentalSerializationApi::class)
fun SettingsContext.toByteArray(): ByteArray = cbor.encodeToByteArray(SettingsContext.serializer(), this)

@OptIn(ExperimentalSerializationApi::class)
fun settingsContextFromByteArray(bytes: ByteArray): SettingsContext =
    cbor.decodeFromByteArray(SettingsContext.serializer(), bytes)

@OptIn(ExperimentalSerializationApi::class)
fun TranslationContext.toByteArray(): ByteArray = cbor.encodeToByteArray(TranslationContext.serializer(), this)

@OptIn(ExperimentalSerializationApi::class)
fun translationContextFromByteArray(bytes: ByteArray): TranslationContext =
    cbor.decodeFromByteArray(TranslationContext.serializer(), bytes)


fun DocumentEntity.toJsonString(): String = json.encodeToString(DocumentEntity.serializer(), this)

fun documentEntityFromJson(json: String): DocumentEntity = decodeFromString<DocumentEntity>(json)