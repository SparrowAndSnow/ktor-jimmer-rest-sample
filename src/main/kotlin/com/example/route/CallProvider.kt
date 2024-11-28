package com.example.route

import com.example.reflect.getTypeByPropertyName
import com.example.reflect.parse
import io.ktor.server.routing.*
import kotlin.reflect.KClass

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

inline fun <reified T : Any, reified E : Any> RoutingCall.toField(name: String): E? {
    val type = getTypeByPropertyName<T>(name) as KClass<*>
    return param(type, name) as E?
}

val RoutingCall.defaultPathVariable
    get() = pathParameters[pathParameters.names().first()]
        ?: throw IllegalStateException("path variable not found")
