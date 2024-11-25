package com.example

import com.example.domain.entity.Book
import com.example.domain.entity.BookDraft
import com.example.domain.entity.copy
import com.example.domain.entity.dto.BookView
import com.example.domain.entity.fetchBy
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.util.JSONPObject
import com.fasterxml.jackson.module.kotlin.jsonMapper
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.resources.Resources
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import org.babyfish.jimmer.Immutable
import org.babyfish.jimmer.View
import org.babyfish.jimmer.client.ApiIgnore
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    install(Resources)
    val sqlClient by inject<KSqlClient>()

    routing {

        get("/book") {
            val list = sqlClient.createQuery(Book::class) {
                select(table.fetchBy {
                    name()
                    edition()
                })
            }.execute()

            val map = list.map {
                BookWithNameUpperCaseView(it)
            }

            call.respond(map)
        }
    }
}

class BookWithNameUpperCaseView(b: Book) : Book by b {
    val nameUpperCase: String? = b.name?.uppercase()

    @JsonIgnore
    override var name: String = b.name
}
