package com.example.route

import org.babyfish.jimmer.sql.kt.ast.query.KMutableQuery

interface FilterProvider<T : Any> {
    var filter: KMutableQuery<T>.() -> Unit

    class Impl<T : Any> : FilterProvider<T> {
        override lateinit var filter: KMutableQuery<T>.() -> Unit
    }
}

inline fun <T : Any> FilterProvider<T>.filter(noinline block: KMutableQuery<T>.() -> Unit) {
    filter = block
}
