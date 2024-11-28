package com.example

import com.example.domain.entity.*
import com.example.route.crud.Create.create
import com.example.route.crud.List.list
import com.example.route.crud.Query.id
import com.example.route.crud.Edit.edit
import com.example.route.crud.Remove.remove
import com.example.route.eq
import com.example.route.fetcher
import com.example.route.filter
import com.example.route.page
import io.ktor.server.application.*
import io.ktor.server.resources.Resources
import io.ktor.server.routing.*
import org.babyfish.jimmer.sql.kt.ast.expression.*

fun Application.configureRouting() {
    install(Resources)

    routing {
        route("/book") {
            id<Book> {}
            list<Book> {
                filter {
                    where(
                        eq(table::name),
                        eq(table::price)
                    )
                    orderBy(table.id.desc())
                }
                fetcher {
                    by {
                        allScalarFields()
                        name(false)
                        nameUpperCase()
                        store {
                            name()
                            website()
                        }
                        authors {
                            name()
                            firstName()
                            lastName()
                        }
                    }
                }
                page {
                    enabled = true
                }
            }
            create<Book> {}
            edit<Book> {}
            remove<Book> {}
        }
    }.getAllRoutes().forEach { log.info("Route: $it") }
}





