package com.example.route

import io.ktor.server.routing.*
import org.babyfish.jimmer.sql.fetcher.Fetcher
import org.babyfish.jimmer.sql.kt.ast.query.KMutableQuery

interface ListProvider<T : Any> :
    FetcherProvider<T>,
    FilterProvider<T>,
    CallProvider {

    class Impl<T : Any>(override var call: RoutingCall) : ListProvider<T> {
        override lateinit var fetcher: Fetcher<T>
        override lateinit var filter: KMutableQuery<T>.() -> Unit
    }
}