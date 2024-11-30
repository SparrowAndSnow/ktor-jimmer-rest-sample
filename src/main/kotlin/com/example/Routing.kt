package com.example

import com.example.domain.entity.*
import com.example.route.*
import com.example.route.crud.Create.create
import com.example.route.crud.List.list
import com.example.route.crud.Query.id
import com.example.route.crud.Edit.edit
import com.example.route.crud.Remove.remove
import io.ktor.server.application.*
import io.ktor.server.resources.Resources
import io.ktor.server.routing.*
import org.babyfish.jimmer.sql.kt.ast.expression.*


fun Application.configureRouting() {
    install(Resources)
    routing {
        route("/book") {
            id<Book> {

            }

            list<Book> {
                filter {
                    where(
                        `ilike?`(table::name),
                        `between?`(table::price),
                        `ilike?`(table.store::name),
                    )
                    where += table.authors {
                        `ilike?`(::lastName)
                    }
                    orderBy(table.id.desc())
                }
                fetcher {
                    by {
                        allScalarFields()
                        name()
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
            }
            create<Book> { body ->
                entity {
                    body.copy {
                        name = body.name.uppercase()
                    }
                }
            }
            edit<Book> {
                validate { entity ->
                    entity::name.notBlank()
                }
            }
            remove<Book> {}
        }
    }.getAllRoutes().forEach { log.info("Route: $it") }
}





