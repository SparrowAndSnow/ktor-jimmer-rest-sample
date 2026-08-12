package com.book

import io.ktor.serialization.jackson3.jackson
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.babyfish.jimmer.jackson.v3.ImmutableModuleV3
import java.math.BigDecimal

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        jackson {
            addModule(ImmutableModuleV3())
        }
    }
}

typealias BigDecimalJson =
    @Serializable(with = BigDecimalSerializer::class)
    BigDecimal

private object BigDecimalSerializer : KSerializer<BigDecimal> {
    override val descriptor = PrimitiveSerialDescriptor("java.math.BigDecimal", PrimitiveKind.DOUBLE)

    override fun deserialize(decoder: Decoder): BigDecimal = decoder.decodeDouble().toBigDecimal()

    override fun serialize(
        encoder: Encoder,
        value: BigDecimal,
    ) = encoder.encodeDouble(value.toDouble())
}
