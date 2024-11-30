package com.example.reflect

import com.example.route.ConditionProvider

class Memoize<in T, in T2, out R>(
    val f: ConditionProvider<*>.(T, T2) -> R,
) : (ConditionProvider<*>, T, T2) -> R {
    private val values = mutableMapOf<T, R>()

    override fun invoke(receiver: ConditionProvider<*>, x: T, x2: T2): R = values.getOrPut(x, { receiver.f(x, x2) })
}

fun <T, T2, R> (ConditionProvider<*>.(T, T2) -> R).memoize(): ConditionProvider<*>.(T, T2) -> R = Memoize(this)

val testMemoize = { x: Any ->
    println("in: $x")
    1
}

fun a() {
    testMemoize

}
