package com.example.route.crud

import com.example.reflect.getPropertyByAnnotation
import com.example.reflect.parse
import com.example.route.CallProvider
import io.ktor.http.*
import io.ktor.server.resources.*
import io.ktor.server.routing.*
import io.ktor.utils.io.*
import org.babyfish.jimmer.sql.Id
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.reflect.KClass

object Remove : KoinComponent {
    val sqlClient by inject<KSqlClient>()

    @KtorDsl
    inline fun <reified TEntity : Any, reified TResource : Any> Route.remove(
        crossinline block: suspend RemoveProvider<TEntity>.(TResource) -> Unit,
    ) = delete<TResource> { resource ->
        val provider = RemoveProvider.Impl<TEntity>(call).apply { block(resource) }

        val pathParameter = call.pathParameters[call.pathParameters.names().first()]
            ?: throw IllegalStateException("path variable not found")

        val key = provider.key
            ?: pathParameter.parse(getPropertyByAnnotation<TEntity>(Id::class)!!
                .returnType.classifier as KClass<*>
            )

        sqlClient.deleteById(TEntity::class, key)
        call.response.status(HttpStatusCode.OK)
    }

    @KtorDsl
    inline fun <reified TEntity : Any> Route.remove(
        path: String = "/{id}",
        crossinline block: suspend RemoveProvider<TEntity>.() -> Unit,
    ) = delete(path) {

        val provider = RemoveProvider.Impl<TEntity>(call).apply { block() }
        val pathParameter = call.pathParameters[call.pathParameters.names().first()]
            ?: throw IllegalStateException("path variable not found")

        val key = provider.key
            ?: pathParameter.parse(getPropertyByAnnotation<TEntity>(Id::class)!!
                .returnType.classifier as KClass<*>
            )

        sqlClient.deleteById(TEntity::class, key)
        call.response.status(HttpStatusCode.OK)
    }
}

interface RemoveProvider<T : Any> :
    CallProvider {
    var key: Any?

    class Impl<T : Any>(
        override val call: RoutingCall,
    ) : RemoveProvider<T> {
        override var key: Any? = null
    }
}

inline fun <reified T : Any> RemoveProvider<T>.key(key: Any) {
    this.key = key
}
