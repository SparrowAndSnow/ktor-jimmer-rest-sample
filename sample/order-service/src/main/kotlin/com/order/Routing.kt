package com.order

import com.order.domain.entity.*
import io.ktor.server.application.*
import io.ktor.server.resources.Resources
import io.ktor.server.routing.*
import com.eimsound.util.jimmer.entityIdType
import com.eimsound.util.ktor.defaultPathVariable
import com.eimsound.util.parser.parse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.response.*
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    install(Resources)
    val httpClient by inject<HttpClient>()

    routing {
        get("/healthCheck"){
            call.response.status(HttpStatusCode.OK)
        }
        route("/book") {
            get("/{id}") {
                val id = call.defaultPathVariable.parse(entityIdType<Book>())
                val response = httpClient.get("http://ktor-sample-book-service/book/$id")
                val body = response.body<Book>()
                call.respond(body)
            }
        }
    }.getAllRoutes().forEach { log.info("Route: $it") }
}





