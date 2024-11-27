package com.example

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.datatype.jsr310.deser.*
import com.fasterxml.jackson.datatype.jsr310.ser.*
import com.fasterxml.jackson.module.kotlin.addDeserializer
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.babyfish.jimmer.jackson.ImmutableModule
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        jackson {
            registerModule(ImmutableModule())
            registerModule(
                JavaTimeModule().apply {
                    addSerializer(LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                    addSerializer(LocalDateSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                    addSerializer(LocalTimeSerializer(DateTimeFormatter.ofPattern("HH:mm:ss")))
                    addDeserializer(LocalDateTime::class, LocalDateTimeDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                    addDeserializer(LocalDate::class, LocalDateDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                    addDeserializer(LocalTime::class, LocalTimeDeserializer(DateTimeFormatter.ofPattern("HH:mm:ss")))
                },
            )
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
