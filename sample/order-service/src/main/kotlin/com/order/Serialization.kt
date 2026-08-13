package com.order

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
import tools.jackson.databind.ext.javatime.deser.LocalDateDeserializer
import tools.jackson.databind.ext.javatime.deser.LocalDateTimeDeserializer
import tools.jackson.databind.ext.javatime.deser.LocalTimeDeserializer
import tools.jackson.databind.ext.javatime.ser.LocalDateSerializer
import tools.jackson.databind.ext.javatime.ser.LocalDateTimeSerializer
import tools.jackson.databind.ext.javatime.ser.LocalTimeSerializer
import tools.jackson.databind.module.SimpleModule
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        jackson {
            addModule(ImmutableModuleV3())
            addModule(timeModule())
        }
    }
}

private fun timeModule() = SimpleModule().apply {
    addSerializer(LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
    addSerializer(LocalDateSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
    addSerializer(LocalTimeSerializer(DateTimeFormatter.ofPattern("HH:mm:ss")))
    addDeserializer(
        LocalDateTime::class.java,
        LocalDateTimeDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
    )
    addDeserializer(LocalDate::class.java, LocalDateDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
    addDeserializer(LocalTime::class.java, LocalTimeDeserializer(DateTimeFormatter.ofPattern("HH:mm:ss")))
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
