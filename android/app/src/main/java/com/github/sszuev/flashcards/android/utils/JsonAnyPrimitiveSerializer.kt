package com.github.sszuev.flashcards.android.utils

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.double
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.int
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive

object JsonAnyPrimitiveSerializer : KSerializer<Any> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("AnyPrimitive") {
        element<String>("string")
        element<Int>("int")
        element<Double>("double")
        element<Boolean>("boolean")
    }

    override fun serialize(encoder: Encoder, value: Any) {
        when (value) {
            is String -> encoder.encodeString(value)
            is Int -> encoder.encodeInt(value)
            is Double -> encoder.encodeDouble(value)
            is Boolean -> encoder.encodeBoolean(value)
            else -> throw SerializationException("Unsupported type: ${value::class}")
        }
    }

    override fun deserialize(decoder: Decoder): Any {
        return when (decoder) {
            is JsonDecoder -> {
                val jsonElement = decoder.decodeJsonElement().jsonPrimitive
                when {
                    jsonElement.isString -> jsonElement.content
                    jsonElement.intOrNull != null -> jsonElement.int
                    jsonElement.doubleOrNull != null -> jsonElement.double
                    jsonElement.booleanOrNull != null -> jsonElement.boolean
                    else -> throw SerializationException("Unknown primitive type")
                }
            }

            else -> throw SerializationException("This serializer can only be used with Json format.")
        }
    }

}