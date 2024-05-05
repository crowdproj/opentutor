package com.gitlab.sszuev.flashcards.utils

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object ThrowableSerializer : KSerializer<Throwable> {
    override val descriptor = PrimitiveSerialDescriptor("Throwable", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Throwable) {
        val data = "${value::class.qualifiedName}|${value.message}"
        encoder.encodeString(data)
    }

    override fun deserialize(decoder: Decoder): Throwable {
        val data = decoder.decodeString()
        val parts = data.split("|", limit = 2)
        val className = parts[0]
        val message = parts.getOrNull(1)
        return createThrowable(className, message)
    }
}