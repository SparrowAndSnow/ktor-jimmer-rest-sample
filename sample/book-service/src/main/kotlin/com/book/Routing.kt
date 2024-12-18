package com.book

import com.eimsound.ktor.provider.*
import com.book.domain.entity.*
import io.ktor.server.application.*
import io.ktor.server.resources.Resources
import io.ktor.server.routing.*
import org.babyfish.jimmer.sql.kt.ast.expression.*
import com.eimsound.ktor.route.*
import com.eimsound.util.ktor.defaultValue
import com.eimsound.util.ktor.queryParameterExt
import java.math.BigDecimal
import com.book.domain.entity.dto.BookInput
import io.ktor.http.*

fun Application.configureRouting() {
    install(Resources)
    routing {
        get("/healthCheck"){
            call.response.status(HttpStatusCode.OK)
        }

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
                validator {
                    it::name.notBlank()
                    it.store!!::name.notBlank()

                    it::price.range(0.toBigDecimal()..100.toBigDecimal()) { name, range ->
                        "$name must be between ${range.start} and ${range.endInclusive}"
                    }

                    it::authors.notEmpty()
                    it.authors.mapIndexed { index, author ->
                        author::firstName.notBlank { name ->
                            "$name[$index] cannot be blank"
                        }
                        author::lastName.notBlank()
                    }

                    it.store!!::website.isUrl()
                }
                entity {
                    it.copy {
                        name = it.name.uppercase()
                    }
                 }
            }
            edit<BookInput> {
                validator {
                    it::name.notBlank()
                }
                entity { body ->
                    BookInput(body.toEntity {
                        name = body.name.uppercase()
                    })
                }
            }
            remove<Book> {}
        }
    }.getAllRoutes().forEach { log.info("Route: $it") }
}





