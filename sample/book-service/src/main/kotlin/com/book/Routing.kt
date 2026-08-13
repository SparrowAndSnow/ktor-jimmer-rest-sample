package com.book

import com.eimsound.ktor.provider.*
import com.book.domain.entity.*
import com.book.domain.entity.dto.BookSpec
//import com.book.domain.entity.dto.BookInput
//import com.book.domain.entity.dto.BookSpec
//import com.book.domain.entity.dto.BookView
import com.eimsound.ktor.route.*
import com.eimsound.util.ktor.get
import com.eimsound.util.ktor.path
import com.eimsound.util.ktor.query
import com.eimsound.util.ktor.specification
import io.ktor.server.application.*
import io.ktor.server.resources.Resources
import io.ktor.server.routing.*
import org.babyfish.jimmer.sql.ast.mutation.SaveMode
import org.babyfish.jimmer.sql.kt.ast.expression.*
import dev.hayden.KHealth
import io.ktor.server.util.getValue

fun Application.configureRouting() {
    install(Resources)
    install(KHealth)
    routing {
        api<Book>("/book") {
//            filter(BookSpec::class){
//                orderBy(table.price.asc())
//            }
            filter {
                val authorFirstName: String? by call.queryParameters
                where(
                    `ilike?`(table::name),           // ?name__start=GraphQL&name__exact=...
                    `in?`(table::edition),           // ?edition=1,2
                    `between?`(table::price)         // ?price__ge=50&price__le=80
                )

//                where(
//                    table.id valueIn subQuery(Author::class) {
//                        where(table.firstName eq "Alex")
//                        select(table.books.id)
//                    }
//                )
                where += table.authors {
                    firstName `ilike?` authorFirstName
                }
                sort()                               // ?sort=price,desc&sort=id,asc
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

            // create/edit 独立配置：保存模式 + 响应投影
            create {
                saveMode = SaveMode.UPSERT           // 同业务键重复提交 = 更新（upsert）
                fetcher {
                    fetch.by {
                        name()
                        edition()
                        price()
                    }
                }
            }
            edit {
                fetcher {
                    fetch.by {
                        name()
                        edition()
                        price()
                    }
                }
            }
            patch { }                                // 启用 PATCH 部分更新（独立配置，默认同 PUT 语义）
            batch {                                  // 启用批量端点（复用 create/edit 配置）
                // path = "bulk"                      // 可自定义路径：POST /book/bulk
                // deleteIdsParameterName = "bookIds" // 批量删除参数名：?bookIds=1,2
            }
        }
    }.getAllRoutes().forEach { log.info("Route: $it") }
}





