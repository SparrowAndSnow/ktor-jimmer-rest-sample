package com.example.route.crud

import com.example.reflect.parse
import com.example.route.*
import io.ktor.http.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.utils.io.*
import org.babyfish.jimmer.sql.fetcher.Fetcher
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

object Query : KoinComponent {
    val sqlClient by inject<KSqlClient>()

    @KtorDsl
    inline fun <reified TEntity : Any, reified TResource : Any> Route.id(
        crossinline block: suspend QueryProvider<TEntity>.(TResource) -> Unit,
    ) = get<TResource> { resource ->
        val provider = QueryProvider.Impl<TEntity>(call).apply { block(resource) }
        val fetcher = provider.fetcher

        val key = provider.key ?: call.defaultPathVariable.parse(entityIdType<TEntity>())

        val result =
            if (fetcher != null) {
                sqlClient.findById(fetcher, key)
            } else {
                sqlClient.findById(TEntity::class, key)
            }

        if (result != null) {
            call.respond(result)
        } else {
            call.response.status(HttpStatusCode.NotFound)
        }
    }

    @KtorDsl
    inline fun <reified TEntity : Any> Route.id(
        path: String = Configuration.defaultPathVariable,
        crossinline block: suspend QueryProvider<TEntity>.() -> Unit,
    ) = get(path) {

        val provider = QueryProvider.Impl<TEntity>(call).apply { block() }
        val fetcher = provider.fetcher

        val key = provider.key ?: call.defaultPathVariable.parse(entityIdType<TEntity>())

        val result =
            if (fetcher != null) {
                sqlClient.findById(fetcher, key)
            } else {
                sqlClient.findById(TEntity::class, key)
            }

        if (result != null) {
            call.respond(result)
        } else {
            call.response.status(HttpStatusCode.NotFound)
        }
    }
}

interface QueryProvider<T : Any> :
    FetcherProvider<T>,
    CallProvider, KeyProvider<T> {

    class Impl<T : Any>(
        override val call: RoutingCall,
    ) : QueryProvider<T> {
        override var fetcher: Fetcher<T>? = null
        override var key: Any? = null
    }
}

inline fun <reified T : Any> QueryProvider<T>.key(key: Any) {
    this.key = key
}
