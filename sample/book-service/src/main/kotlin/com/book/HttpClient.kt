package com.book

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer
import com.fasterxml.jackson.module.kotlin.addDeserializer
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import org.babyfish.jimmer.jackson.ImmutableModule
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

fun httpClient(environment: ApplicationEnvironment ): HttpClient {
    val client = HttpClient(CIO) {
        install(ConsulFeature) {
            consulUrl = environment.config.property("consul.url").getString()
        }
        install(ContentNegotiation){
            jackson{
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
    return client
}
