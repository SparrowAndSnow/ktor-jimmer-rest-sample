package com.example.route

import org.babyfish.jimmer.sql.kt.ast.query.KMutableRootQuery

interface FilterProvider<T : Any> {
    var filter: (KMutableRootQuery<T>.() -> Unit)?

    class Impl<T : Any> : FilterProvider<T> {
        override var filter: (KMutableRootQuery<T>.() -> Unit)? = null
    }
}

inline fun <T : Any> FilterProvider<T>.filter(noinline block: KMutableRootQuery<T>.() -> Unit) {
    filter = block
}
