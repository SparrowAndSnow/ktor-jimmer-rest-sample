package com.example

import com.eimsound.ktor.provider.*
import com.example.domain.entity.*
import io.ktor.server.application.*
import io.ktor.server.resources.Resources
import io.ktor.server.routing.*
import org.babyfish.jimmer.sql.kt.ast.expression.*
import com.eimsound.ktor.route.*
import com.eimsound.util.ktor.defaultValue
import com.eimsound.util.ktor.queryParameterExt
import java.math.BigDecimal

fun Application.configureRouting() {
    install(Resources)
    routing {
        route("/book") {
            id<Book> {}

            list<Book> {
                filter {
                    where(
                        `ilike?`(table::name),
                        `ilike?`(table.store::name),
                        `between?`(table::price)
                    )

                    val price = call.queryParameterExt<BigDecimal>("price")
                    where(
                        table.price.`between?`(
                            price["le"]?.value,
                            price["ge"]?.value
                        )
                    )

                    where += table.authors {
                        firstName `ilike?` call.queryParameterExt<String>("firstName").defaultValue()
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
            create<Book> {
                validator { body ->
                    body::name.notBlank()
                    body.store!!::name.notBlank()

                    body::price.range(0.toBigDecimal() .. 100.toBigDecimal()){ name, range ->
                        "$name must be between ${range.start} and ${range.endInclusive}"
                    }

                    body::authors.notEmpty()
                    body.authors.mapIndexed { index, author ->
                        author::firstName.notBlank { name ->
                            "$name[$index] cannot be blank"
                        }
                        author::lastName.notBlank()
                    }

                    body.store!!::website.isUrl()
                }
                entity { body ->
                    body.copy {
                        name = body.name.uppercase()
                    }
                }
            }
            edit<Book> {
                validator { body ->
                    body::name.notBlank()
                }
            }
            remove<Book> {}
        }
    }.getAllRoutes().forEach { log.info("Route: $it") }
}





