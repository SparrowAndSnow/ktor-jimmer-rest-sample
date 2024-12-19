package com.order

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*


fun httpClient(environment: ApplicationEnvironment ): HttpClient {
    val client = HttpClient(CIO) {
        install(ConsulFeature) {
            consulUrl = environment.config.property("consul.url").getString()
        }
        install(ContentNegotiation){
            jackson{
                registerModule()
            }
        }
    }
    return client
}
