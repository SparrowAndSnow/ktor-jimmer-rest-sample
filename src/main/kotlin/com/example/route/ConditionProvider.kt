package com.example.route

import io.ktor.server.routing.*
import org.babyfish.jimmer.sql.kt.ast.expression.*
import kotlin.reflect.KProperty0


interface ConditionProvider<T : Any> {
    val call: RoutingCall

    class Impl<T : Any>(override val call: RoutingCall) : ConditionProvider<T> {}
}

inline fun <reified T : Any, reified P : Any> ConditionProvider<T>.eq(field: KProperty0<KExpression<P>>)
    : KNonNullExpression<Boolean>? {
    return field.invoke() `eq?` call.toField<T, P>(field.name)
}
