package com.order

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.jackson3.jackson
import io.ktor.server.application.*
import org.babyfish.jimmer.jackson.v3.ImmutableModuleV3

fun httpClient(environment: ApplicationEnvironment): HttpClient {
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            jackson {
                addModule(ImmutableModuleV3())
            }
        }
    }
    return client
}
