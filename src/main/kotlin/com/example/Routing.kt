package com.example

import com.example.domain.entity.*
import com.example.route.Curd.list
import com.example.route.Curd.id
import com.example.route.fetcher
import com.example.route.filter
import io.ktor.server.application.*
import io.ktor.server.resources.Resources
import io.ktor.server.routing.*
import org.babyfish.jimmer.sql.kt.ast.expression.*
import org.babyfish.jimmer.sql.kt.fetcher.newFetcher

fun Application.configureRouting() {
    install(Resources)

    routing {
        route("/book") {
            id<Book>("key") {
                fetcher(Book::class) {
                    by {
                        allScalarFields()
                        store {
                            allScalarFields()
                        }
                        authors {
                            allScalarFields()
                        }
                        name(false)
                    }
                }
            }

            list<Book> {
                fetcher(Book::class) {
                    by { allScalarFields() }
                }
                filter {
                    where(
                        table.name `like?` call.queryParameters["name"],
                        table.price `ge?` call.queryParameters["price"]?.toBigDecimal()
                    )
                    orderBy(table.price.asc())
                }
            }
        }
    }
}

val bookFetcher = newFetcher(Book::class).by {
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
