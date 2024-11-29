package com.example.route.crud

import com.example.reflect.parse
import com.example.route.CallProvider
import com.example.route.KeyProvider
import com.example.route.defaultPathVariable
import com.example.route.entityIdType
import io.ktor.http.*
import io.ktor.server.resources.*
import io.ktor.server.routing.*
import io.ktor.utils.io.*
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

object Remove : KoinComponent {
    val sqlClient by inject<KSqlClient>()

    @KtorDsl
    inline fun <reified TEntity : Any, reified TResource : Any> Route.remove(
        crossinline block: suspend RemoveProvider<TEntity>.(TResource) -> Unit,
    ) = delete<TResource> { resource ->
        val provider = RemoveProvider.Impl<TEntity>(call).apply { block(resource) }

        val key = provider.key ?: call.defaultPathVariable.parse(entityIdType<TEntity>())

        sqlClient.deleteById(TEntity::class, key)
        call.response.status(HttpStatusCode.OK)
    }

    @KtorDsl
    inline fun <reified TEntity : Any> Route.remove(
        path: String = Configuration.defaultPathVariable,
        crossinline block: suspend RemoveProvider<TEntity>.() -> Unit,
    ) = delete(path) {
        val provider = RemoveProvider.Impl<TEntity>(call).apply { block() }
        val key = provider.key ?: call.defaultPathVariable.parse(entityIdType<TEntity>())

        sqlClient.deleteById(TEntity::class, key)
        call.response.status(HttpStatusCode.OK)
    }
}

interface RemoveProvider<T : Any> :
    CallProvider, KeyProvider<T> {
    class Impl<T : Any>(
        override val call: RoutingCall,
    ) : RemoveProvider<T> {
        override var key: Any? = null
    }
}


