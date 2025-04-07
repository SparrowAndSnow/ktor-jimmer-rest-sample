package com.book

import com.eimsound.ktor.provider.*
import com.book.domain.entity.*
import com.book.domain.entity.dto.BookInput
import com.book.domain.entity.dto.BookSpec
import com.book.domain.entity.dto.BookView
import io.ktor.server.application.*
import io.ktor.server.resources.Resources
import io.ktor.server.routing.*
import org.babyfish.jimmer.sql.kt.ast.expression.*
import com.eimsound.ktor.route.*
import com.eimsound.util.ktor.defaultValue
import com.eimsound.util.ktor.queryParameterExt
import dev.hayden.KHealth

fun Application.configureRouting() {
    install(Resources)
    install(KHealth)
    routing {
        create<BookInput>("/book/create") {
            validator {
                with(it) {
                    name.notBlank { "姓名不能为空" }
                    price.range(0.toBigDecimal()..100.toBigDecimal()) { range ->
                        "价格必须在${range.start}和${range.endInclusive}之间"
                    }
                }
            }
            entity {
                it.copy(name = it.name.uppercase())
            }
        }
        api<Book, BookInput>("/book") {
            filter {
//                where(specification<BookSpec>())
                where(
                    table.name `ilike?` call.queryParameterExt<String>("name").defaultValue(),
                    `between?`(table::price),
                )

                where += table.authors {
                    firstName `ilike?` this@api.call.queryParameterExt<String>("firstName").defaultValue()
                }
                orderBy(table.id.desc())
            }
//            fetcher(BookView::class)
            fetcher {
                creator.by {
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
            validator {
                with(it) {
                    name.notBlank { "姓名不能为空" }
                    price.range(0.toBigDecimal()..100.toBigDecimal()) { range ->
                        "价格必须在${range.start}和${range.endInclusive}之间"
                    }
                }
            }
            entity {
                it.copy(name = it.name.uppercase())
            }
        }
    }.getAllRoutes().forEach { log.info("Route: $it") }
}





