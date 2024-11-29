package com.example.route.crud

import com.example.route.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.utils.io.*
import org.babyfish.jimmer.sql.fetcher.Fetcher
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.ast.query.KMutableQuery
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

object List : KoinComponent {
    val sqlClient by inject<KSqlClient>()

    @KtorDsl
    inline fun <reified TEntity : Any, reified TResource : Any> Route.list(
        crossinline block: suspend ListProvider<TEntity>.(TResource) -> Unit,
    ) = get<TResource> { resource ->
        val provider = ListProvider.Impl<TEntity>(call).apply { block(resource) }
        val page = provider.page
        val filter = provider.filter
        val fetcher = provider.fetcher

        val result =
            sqlClient
                .createQuery(TEntity::class) {
                    filter?.invoke(this)
                    select(table.fetch(fetcher))
                }.let {
                    when {
                        page.enabled -> it.fetchPage(
                            call.param<Int>(page::pageIndex.name) ?: page.pageIndex,
                            call.param<Int>(page::pageSize.name) ?: page.pageSize
                        )
                        else -> it.execute()
                    }
                }

        call.respond(result)
    }


    @KtorDsl
    inline fun <reified TEntity : Any> Route.list(
        crossinline block: suspend ListProvider<TEntity>.() -> Unit,
    ) = get {
        val provider = ListProvider.Impl<TEntity>(call).apply { block() }
        val page = provider.page
        val filter = provider.filter
        val fetcher = provider.fetcher

        val result =
            sqlClient
                .createQuery(TEntity::class) {
                    filter?.invoke(this)
                    select(table.fetch(fetcher))
                }.let {
                    when {
                        page.enabled -> it.fetchPage(
                            call.param<Int>(page::pageIndex.name) ?: page.pageIndex,
                            call.param<Int>(page::pageSize.name) ?: page.pageSize
                        )

                        else -> it.execute()
                    }
                }

        call.respond(result)
    }
}

interface ListProvider<T : Any> :
    FetcherProvider<T>,
    FilterProvider<T>,
    PageProvider,
    CallProvider,
    ConditionProvider<T> {
    class Impl<T : Any>(
        override val call: RoutingCall,
    ) : ListProvider<T> {
        override var fetcher: Fetcher<T>? = null
        override var filter: (KMutableQuery<T>.() -> Unit)? = null
        override var page: Page = Page()
    }
}
