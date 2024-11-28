package com.example

import com.example.domain.entity.*
import com.example.route.crud.Create.create
import com.example.route.crud.List.list
import com.example.route.crud.Query.id
import com.example.route.crud.Edit.edit
import com.example.route.crud.Remove.remove
import com.example.route.filter
import io.ktor.resources.*
import io.ktor.server.application.*
import io.ktor.server.resources.Resources
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.babyfish.jimmer.sql.kt.ast.expression.*

fun Application.configureRouting() {
    install(Resources)

    routing {
        route("/book"){
            id<Book> {}
            list<Book, BookRoute> { query ->
                filter {
                    where(
                        table.name `like?` query.name,
                        table.price `ge?` query.price,
                    )
                }
            }
            create<Book>{}
            edit<Book> {}
            remove<Book> {}
        }
    }.getAllRoutes().forEach { log.info("Route: $it") }
}

@Serializable
@Resource("/")
class BookRoute(
    val name: String? = null,
    val price: BigDecimalJson? = null,
)

