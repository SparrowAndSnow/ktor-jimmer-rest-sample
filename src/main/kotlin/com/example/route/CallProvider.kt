package com.example.route

import com.example.reflect.getTypeByPropertyName
import com.example.reflect.parse
import com.example.route.crud.Configuration
import io.ktor.server.routing.*
import io.ktor.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0

interface CallProvider {
    val call: RoutingCall
}

inline fun <T : Any> RoutingCall.param(type: KClass<T>, name: String): T? {
    val serializable = queryParameters[name]?.parse(type)
    return serializable as T?
}

inline fun <reified T : Any> RoutingCall.param(name: String): T? {
    return param(T::class, name)
}

inline fun <reified T : Any, reified E : Any> RoutingCall.param(parameter: Parameter<E>): E? {
    val type = getTypeByPropertyName<T, E>(parameter.name)
    return param(type, parameter.nameWithExt) as E?
}

val RoutingCall.defaultPathVariable
    get() = pathParameters[pathParameters.names().first()]
        ?: throw IllegalStateException("path variable not found")


/**
 * Create a condition according to the HTTP parameter.
 *
 * The HTTP parameter name is the property name of the entity,
 * and the HTTP parameter value is the value of the condition.
 * The condition is `eq` by default.
 * If the HTTP parameter name is suffixed with a special character,
 * that special character is used as the operator of the condition.
 * The supported special characters are `gt`, `lt`, `ge`, `le`.
 * For example, if the HTTP parameter is `name_gt=abc`,
 * the condition is `name gt 'abc'`.
 *
 * @param out T
 * @property kProperty0 KProperty0<T>
 * @property ext String?
 * @property separator String
 * @property nameWithExt String
 * @constructor
 */
class Parameter<T>(val property: KProperty<T>) : KProperty<T> by property {
    // 比如 ge le exact
    var ext: String? = null
    val separator get() = Configuration.parameterSeparator
    val hasExt get() = ext != null

    // 比如 createTime__ge createTime__le name__exact
    val nameWithExt: String get() = property.name + if (hasExt) separator + ext else ""

    var value: T? = null
}

infix fun <T> KProperty0<T>.ext(ext: String): Parameter<T> =
    Parameter(this).apply {
        this.ext = ext
    }
