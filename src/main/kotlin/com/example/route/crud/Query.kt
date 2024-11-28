package com.example.route.crud

import com.example.reflect.getPropertyByAnnotation
import com.example.reflect.parse
import com.example.route.CallProvider
import com.example.route.FetcherProvider
import io.ktor.http.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.utils.io.*
import org.babyfish.jimmer.sql.Id
import org.babyfish.jimmer.sql.fetcher.Fetcher
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.reflect.KClass

object Query : KoinComponent {
    val sqlClient by inject<KSqlClient>()

    @KtorDsl
    inline fun <reified TEntity : Any, reified TResource : Any> Route.id(
        crossinline block: suspend QueryProvider<TEntity>.(TResource) -> Unit,
    ) = get<TResource> { resource ->
        val provider = QueryProvider.Impl<TEntity>(call).apply { block(resource) }
        val fetcher = provider.fetcher

        val pathParameter = call.pathParameters[call.pathParameters.names().first()]
            ?: throw IllegalStateException("path variable not found")

        val key = provider.key
            ?: pathParameter.parse(getPropertyByAnnotation<TEntity>(Id::class)!!
                .returnType.classifier as KClass<*>)

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
        path: String = "/{id}",
        crossinline block: suspend QueryProvider<TEntity>.() -> Unit,
    ) = get(path) {


        val provider = QueryProvider.Impl<TEntity>(call).apply { block() }
        val fetcher = provider.fetcher

        val pathParameter = call.pathParameters[call.pathParameters.names().first()]
            ?: throw IllegalStateException("path variable not found")

        val key = provider.key
            ?: pathParameter.parse(getPropertyByAnnotation<TEntity>(Id::class)!!
                .returnType.classifier as KClass<*>)

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
    CallProvider {
    var key: Any?

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
