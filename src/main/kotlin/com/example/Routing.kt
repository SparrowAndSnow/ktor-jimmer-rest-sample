package com.example

import com.example.domain.entity.*
import io.ktor.server.application.*
import io.ktor.server.resources.Resources
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.ast.expression.asc
import org.babyfish.jimmer.sql.kt.ast.expression.desc
import org.babyfish.jimmer.sql.kt.fetcher.newFetcher
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    install(Resources)
    val sqlClient by inject<KSqlClient>()

    routing {
        route("/book") {
            get {
                val list = sqlClient.createQuery(Book::class) {
                    orderBy(table.price.asc(), table.id.desc())
                    select(table.fetch(bookFetcher()))
                }.fetchPage(0, 10)
                call.respond(list)
            }

            get("/{id}") {
                call.parameters["id"]?.toLong()?.let {
                    sqlClient.findById(bookFetcher(), it)?.let {
                        call.respond(it)
                    }
                }
            }
        }
    }
}

fun bookFetcher() = newFetcher(Book::class).by {
    nameUpperCase()
    price()
    env()
    store {
        name()
        website()
    }
    authors {
        name()
        gender()
    }
}
