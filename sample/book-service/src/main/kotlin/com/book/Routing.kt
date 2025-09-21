package com.book

import com.eimsound.ktor.provider.*
import com.book.domain.entity.*
//import com.book.domain.entity.dto.BookInput
//import com.book.domain.entity.dto.BookSpec
//import com.book.domain.entity.dto.BookView
import com.eimsound.ktor.route.api
import io.ktor.server.application.*
import io.ktor.server.resources.Resources
import io.ktor.server.routing.*
import org.babyfish.jimmer.sql.kt.ast.expression.*
import dev.hayden.KHealth

fun Application.configureRouting() {
    install(Resources)
    install(KHealth)
    routing {
        api<Book>("/book") {
//            filter(BookSpec::class){
//                orderBy(table.price.asc())
//            }
            filter {
                where(
                    table.name `ilike?` get("name"),
                    table.price.`between?`(
                        get("price", "ge"),
                        this["price", "le"]
                    ),
                    `between?`(table::edition)
                )

//                where(
//                    table.id valueIn subQuery(Author::class) {
//                        where(table.firstName eq "Alex")
//                        select(table.books.id)
//                    }
//                )

                where += table.authors {
                    firstName `ilike?` this@filter["firstName"]
                }
                orderBy(table.id.desc())
            }
//            fetcher(BookView::class)
            fetcher {
                fetch.by {
                    name()
                    edition()
                    price()
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

//            input(BookInput::class) {}
            input {
                validator {
                    with(it) {
                        ::name.notBlank { "名称不能为空" }
                        ::price.range(0.toBigDecimal()..100.toBigDecimal()) { range ->
                            "价格必须在${range.start}和${range.endInclusive}之间"
                        }
                    }
                }
                transformer {
                    it.copy { name = it.name.uppercase() }
                }
            }
        }
    }.getAllRoutes().forEach { log.info("Route: $it") }
}





