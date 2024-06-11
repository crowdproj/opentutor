package com.gitlab.sszuev.flashcards.utils

import kotlinx.serialization.KSerializer
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder

object MapStringAnySerializer : KSerializer<Map<String, Any>> {
    private val jsonMapDelegateSerializer = MapSerializer(String.serializer(), JsonAnyPrimitiveSerializer)
    private val cborMapDelegateSerializer = MapSerializer(String.serializer(), PolymorphicSerializer(Any::class))

    override val descriptor: SerialDescriptor = jsonMapDelegateSerializer.descriptor

    override fun serialize(encoder: Encoder, value: Map<String, Any>) = if (encoder is JsonEncoder) {
        jsonMapDelegateSerializer.serialize(encoder, value)
    } else {
        cborMapDelegateSerializer.serialize(encoder, value)
    }

    override fun deserialize(decoder: Decoder): Map<String, Any> = if (decoder is JsonDecoder) {
        jsonMapDelegateSerializer.deserialize(decoder)
    } else {
        cborMapDelegateSerializer.deserialize(decoder)
    }
}