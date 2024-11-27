package com.example.route.crud

import com.example.route.CallProvider
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.utils.io.*
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

object Create : KoinComponent {
    val sqlClient by inject<KSqlClient>()

    @KtorDsl
    inline fun <reified TEntity : Any, reified TResource : Any> Route.create(
        crossinline block: suspend CreateProvider<TEntity>.(TResource) -> Unit,
    ) = post<TResource> {
        val resource = call.receive<TResource>()

        val provider = CreateProvider.Impl<TEntity>(call).apply { block(resource) }

        val result = sqlClient.insert(resource)
    }
}

interface CreateProvider<T : Any> : CallProvider {
    class Impl<T : Any>(
        override val call: RoutingCall,
    ) : CreateProvider<T>
}
