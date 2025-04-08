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
import dev.hayden.KHealth
import java.math.BigDecimal

fun Application.configureRouting() {
    install(Resources)
    install(KHealth)
    routing {
        api<Book>("/book") {
            filter {
                where(specification<BookSpec>())
                where(
                    table.name `ilike?` param<String>("name"),

                    table.price.`between?`(
                        param<BigDecimal>("price", "ge"),
                        param<BigDecimal>("price", "le")
                    )
//                    `between?`(table::price),
                )

                where += table.authors {
                    firstName `ilike?` this@filter.param<String>("firstName")
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
                    ::name.notNull { "姓名不能为空" }
                    ::price.range(0.toBigDecimal()..100.toBigDecimal()) { range ->
                        "价格必须在${range.start}和${range.endInclusive}之间"
                    }
                }
            }
        }
    }.getAllRoutes().forEach { log.info("Route: $it") }
}





