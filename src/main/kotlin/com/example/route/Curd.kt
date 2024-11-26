package com.example.route


import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.utils.io.*
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

object Curd : KoinComponent {
    val sqlClient by inject<KSqlClient>()

    @KtorDsl
    inline fun <reified T : Any> Route.id(path: String? = null, crossinline query: FetcherProvider<T>.() -> Unit): Any {
        val defaultParameter = "id"

        return get("/{${path ?: defaultParameter}}") {
            val queryProvider = FetcherProvider.Impl<T>().apply(query)

            call.parameters[path ?: defaultParameter]?.toLong()?.let {
                if (queryProvider.fetcher != null) {
                    sqlClient.findById(queryProvider.fetcher, it)
                } else {
                    sqlClient.findById(T::class, it)
                }?.let {
                    call.respond(it)
                }
            }
        }
    }


    @KtorDsl
    inline fun <reified T : Any> Route.list(crossinline query: ListProvider<T>.() -> Unit) = get {
        val queryProvider = ListProvider.Impl<T>(call).apply(query)

        val list = sqlClient.createQuery(T::class) {
            queryProvider.filter(this)
            select(table.fetch(queryProvider.fetcher))
        }.fetchPage(0, 10)

        call.respond(list)
    }
}
