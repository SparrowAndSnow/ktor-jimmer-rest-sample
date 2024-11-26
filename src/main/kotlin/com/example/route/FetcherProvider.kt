package com.example.route

import org.babyfish.jimmer.sql.fetcher.Fetcher
import org.babyfish.jimmer.sql.kt.fetcher.FetcherCreator
import org.babyfish.jimmer.sql.kt.fetcher.newFetcher
import kotlin.reflect.KClass

interface FetcherProvider<T : Any> {
    var fetcher: Fetcher<T>

    class Impl<T : Any> : FetcherProvider<T> {
        override lateinit var fetcher: Fetcher<T>
    }
}

inline fun <T : Any> FetcherProvider<T>.fetcher(clazz: KClass<T>, block: FetcherCreator<T>.() -> Fetcher<T>) {
    fetcher = block(newFetcher(clazz))
}