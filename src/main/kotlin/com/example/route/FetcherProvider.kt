package com.example.route

import org.babyfish.jimmer.sql.fetcher.Fetcher
import org.babyfish.jimmer.sql.kt.fetcher.FetcherCreator
import org.babyfish.jimmer.sql.kt.fetcher.newFetcher

interface FetcherProvider<T : Any> {
    var fetcher: Fetcher<T>?

    class Impl<T : Any> : FetcherProvider<T> {
        override var fetcher: Fetcher<T>? = null
    }
}

inline fun <reified T : Any> FetcherProvider<T>.fetcher(block: FetcherCreator<T>.() -> Fetcher<T>) {
    fetcher = block(newFetcher(T::class))
}
