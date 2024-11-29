package com.example.route

import com.example.reflect.getPropertyByPropertyName
import com.example.route.crud.Configuration
import io.ktor.server.routing.*
import org.babyfish.jimmer.sql.ast.LikeMode
import org.babyfish.jimmer.sql.kt.ast.expression.*
import kotlin.reflect.KProperty0


interface ConditionProvider<T : Any> {
    val call: RoutingCall

    class Impl<T : Any>(override val call: RoutingCall) : ConditionProvider<T> {
    }
}

fun <T : Any> ConditionProvider<T>.findNameWithExt(name: String): List<Pair<String, String?>> {
    val names = call.queryParameters.names()
    val list = names.filter { it.startsWith(name) }.map {
        val parameter = it.split(Configuration.parameterSeparator)
        parameter.get(0) to parameter.getOrNull(1)
    }
    return list
}


inline fun <reified T : Any, reified P : Any> ConditionProvider<T>.parameter(
    name: String,
    ext: String? = null
): Map<String?, Parameter<P>?> {
    val findNameWithExt = findNameWithExt(name)
    val map = findNameWithExt.map {
        val (parameterName, parameterExt) = it
        val property = getPropertyByPropertyName<T, P>(parameterName)

        if (property == null) return@map (parameterExt ?: ext) to null

        val parameter = Parameter(property).apply {
            this.ext = parameterExt ?: ext
            this.value = call.param<T, P>(this)
        }
        return@map parameter.ext to parameter
    }.toMap()
    return map
}


inline fun <reified T : Any, reified P : Any> ConditionProvider<T>.`eq?`(param: KProperty0<KExpression<P>>)
    : KNonNullExpression<Boolean>? {
    val parameter = parameter<T, P>(param.name).values.firstOrNull()
    return param?.invoke()?.`eq?`(parameter?.value)
}


@Suppress("ktlint:standard:function-naming")
inline fun <reified T : Any> ConditionProvider<T>.`ilike?`(
    param: KProperty0<KExpression<String>>
): KNonNullExpression<Boolean>? {
    val parameter = parameter<T, String>(param.name)?.values?.firstOrNull()
    val likeMode = when (parameter?.ext) {
        "anywhere" -> LikeMode.ANYWHERE
        "exact" -> LikeMode.EXACT
        "start" -> LikeMode.START
        "end" -> LikeMode.END
        else -> LikeMode.ANYWHERE
    }
    return param?.invoke()?.`ilike?`(parameter?.value, likeMode)
}

inline fun <reified T : Any, reified P : Comparable<*>> ConditionProvider<T>.`between?`(
    param: KProperty0<KExpression<P>>,
): KNonNullExpression<Boolean>? {
    val parameter = parameter<T, P>(param.name)
    return param?.invoke()?.`between?`(parameter["ge"]?.value, parameter["le"]?.value)
}


