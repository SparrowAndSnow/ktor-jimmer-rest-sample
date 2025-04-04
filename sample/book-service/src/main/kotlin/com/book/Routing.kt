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
import com.book.domain.entity.dto.BookInput
import com.book.domain.entity.dto.BookSpec
import dev.hayden.KHealth

fun Application.configureRouting() {
    install(Resources)
    install(KHealth)
    routing {
        route("/book") {
            id<Book> {}

            list<Book> {
                filter {
                    where(specification<BookSpec>())
                    where(
                        `ilike?`(table::name),
                        `ilike?`(table.store::name),
                        `between?`(table::price)
                    )

                    where += table.authors {
                        firstName `ilike?` call.queryParameterExt<String>("firstName").defaultValue()
                    }
                    orderBy(table.id.desc())
                }
                fetcher {
                    creator.by {
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





