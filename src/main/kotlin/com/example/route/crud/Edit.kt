package com.example.route.crud

import com.example.route.CallProvider
import com.example.route.EntityProvider
import io.ktor.server.request.*
import io.ktor.server.resources.patch
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.utils.io.*
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

object Edit : KoinComponent {
    val sqlClient by inject<KSqlClient>()

    @KtorDsl
    inline fun <reified TEntity : Any, reified TResource : Any> Route.edit(
        crossinline block: suspend EditProvider<TEntity>.(TEntity) -> Unit,
    ) = patch<TResource> {
        process(block)
    }

    @KtorDsl
    inline fun <reified TEntity : Any> Route.edit(
        path: String = "",
        crossinline block: suspend EditProvider<TEntity>.(TEntity) -> Unit,
    ) = patch(path) {
        process(block)
    }

    inline suspend fun <reified TEntity : Any> RoutingContext.process(
        crossinline block: suspend EditProvider<TEntity>.(TEntity) -> Unit
    ) {
        val body = call.receive<TEntity>()
        val provider = EditProvider.Impl<TEntity>(call).apply { block(body) }
        val entity = provider.entity ?: body
        entity?.let {
            val result = sqlClient.update(it)
            call.respond(result.modifiedEntity)
        }
    }
}

interface EditProvider<T : Any> : CallProvider, EntityProvider<T> {
    class Impl<T : Any>(
        override val call: RoutingCall,
    ) : EditProvider<T> {
        override var entity: T? = null
    }
}
