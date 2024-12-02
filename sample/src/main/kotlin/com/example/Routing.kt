package com.example

import com.eimsound.ktor.jimmer.rest.provider.*
import com.example.domain.entity.*
import io.ktor.server.application.*
import io.ktor.server.resources.Resources
import io.ktor.server.routing.*
import org.babyfish.jimmer.sql.kt.ast.expression.*
import com.eimsound.ktor.jimmer.rest.route.*

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
                validate { entity ->
                    entity::name.notBlank()
                }
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





