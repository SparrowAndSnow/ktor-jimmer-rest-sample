package com.example

import com.example.domain.entity.*
import io.ktor.server.application.*
import io.ktor.server.resources.Resources
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    install(Resources)
    val sqlClient by inject<KSqlClient>()

    routing {
        route("/book"){
            get {
                val list = sqlClient.createQuery(Book::class) {
                    select(table.fetchBy {
                        nameUpperCase()
                        env()
                    })
                }.fetchPage(1, 10)
                call.respond(list)
            }
            get("/{id}") {
                call.parameters["id"]?.toLong()?.let {
                    sqlClient.findById(Book::class, it)?.let {
                        call.respond(it)
                    }
                }
            }
        }
    }
}
