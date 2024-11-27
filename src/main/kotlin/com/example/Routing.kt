package com.example

import com.example.domain.entity.*
import com.example.route.crud.Create.create
import com.example.route.crud.List.list
import com.example.route.crud.Query.id
import com.example.route.crud.key
import com.example.route.fetcher
import com.example.route.filter
import io.ktor.resources.*
import io.ktor.server.application.*
import io.ktor.server.resources.Resources
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.babyfish.jimmer.sql.kt.ast.expression.*
import org.babyfish.jimmer.sql.kt.fetcher.newFetcher
import java.math.BigDecimal

fun Application.configureRouting() {
    install(Resources)

    routing {
        id<Book, BookRoute.Id> { param ->
            key(param.id)
            fetcher {
                by {
                    allScalarFields()
                    nameUpperCase()
                    name(false)
                    store {
                        allScalarFields()
                    }
                    authors {
                        allScalarFields()
                    }
                }
            }
        }

        list<Book, BookRoute> { query ->
            fetcher {
                by {
                    nameUpperCase()
                    name(false)
                }
            }
            filter {
                where(
                    table.name `like?` query.name,
                    table.price `ge?` query.price,
                )
                orderBy(table.price.asc())
            }
        }

        create<Book, BookRoute.Create> { body ->
        }
    }
}

val envFetcher =
    newFetcher(Book::class).by {
        env()
    }

@Serializable
@Resource("/book")
class BookRoute(
    val name: String? = null,
    val price: BigDecimalJson? = null,
) {
    @Serializable
    @Resource("{id}")
    class Id(
        val parent: BookRoute = BookRoute(),
        val id: Long,
    )

    @Serializable
    @Resource("/create")
    class Create(
        override val id: Long,
        override val name: String,
        override val edition: Int,
        override val price: BigDecimalJson,
        override val store: BookStore?,
        override val authors: List<Author>,
    ) : Book {
        override val env: Map<String, Any?>
            get() = TODO("Not yet implemented")
    }
}
