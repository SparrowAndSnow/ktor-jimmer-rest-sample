package com.example.route

import com.example.reflect.getPropertyByPropertyName
import com.example.route.crud.Configuration
import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.util.*
import org.babyfish.jimmer.sql.kt.ast.expression.*
import kotlin.reflect.KProperty0


interface ConditionProvider<T : Any> {
    val call: RoutingCall

    class Impl<T : Any>(override val call: RoutingCall) : ConditionProvider<T> {
    }
}

inline val <reified T : Any> ConditionProvider<T>.parameters: Map<String, Parameter<*>?>
    get() = call.queryParameters.toMap().map {
        val (name, ext) = it.key.split(Configuration.parameterSeparator)
        val parameter = getPropertyByPropertyName(T::class, it.key)?.let {
            Parameter(it).apply { ext }
        }
        name to parameter
    }.toMap()

inline fun <reified T : Any, reified P : Any> ConditionProvider<T>.eq(param: KProperty0<KExpression<P>>)
    : KNonNullExpression<Boolean>? {
    val parameter = parameters[param.name]

    return param?.invoke()?.`eq`(call.toField<T, P>(parameter?.name!!))
}

inline fun <reified P : Any> eq(
    param: KProperty0<KExpression<P>>,
    param2: KProperty0<KExpression<P>>
): KNonNullExpression<Boolean>? {
    return param?.invoke()?.`eq`(param2.invoke())
}

@Suppress("ktlint:standard:function-naming")
inline fun <reified T : Any> ConditionProvider<T>.`ilike?`(
    param: KProperty0<KExpression<String>>
): KNonNullExpression<Boolean>? {

    return param?.invoke()?.`ilike?`(call.toField<T, String>(param.name))
}

inline fun <reified T : Any, reified P : Comparable<*>> ConditionProvider<T>.between(
    param: KProperty0<KExpression<P>>,
    param2: KProperty0<KExpression<P>>
): KNonNullExpression<Boolean>? {
    return param?.invoke()?.`between?`(call.toField<T, P>(param.name), call.toField<T, P>(param2.name))
}


