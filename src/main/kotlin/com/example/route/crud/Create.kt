package com.example.route.crud

import com.example.route.CallProvider
import com.example.route.EntityProvider
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.utils.io.*
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import io.ktor.server.resources.post

object Create : KoinComponent {
    val sqlClient by inject<KSqlClient>()

    @KtorDsl
    inline fun <reified TEntity : Any, reified TResource : Any> Route.create(
        crossinline block: suspend CreateProvider<TEntity>.(TEntity) -> Unit,
    ) = post<TResource> {
        val body = call.receive<TEntity>()
        val provider = CreateProvider.Impl<TEntity>(call).apply { block(body) }
        val entity = provider.entity ?: body
        entity?.let {
            val result = sqlClient.insert(it)
            call.respond(result.modifiedEntity)
        }
    }
}

interface CreateProvider<T : Any> : CallProvider, EntityProvider<T> {
    class Impl<T : Any>(
        override val call: RoutingCall,
    ) : CreateProvider<T> {
        override var entity: T? = null
    }
}

