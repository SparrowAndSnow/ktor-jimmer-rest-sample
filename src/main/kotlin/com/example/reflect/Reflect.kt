package com.example.reflect

import java.io.Serializable
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

inline fun <reified T : Any> getPropertyByAnnotation(annotation: KClass<*>) =
    T::class.memberProperties.find { it.annotations.any { it.annotationClass == annotation } }

inline fun String.parse(type: KClass<*>): Serializable = when (type) {
    Int::class -> this.toInt()
    Long::class -> this.toLong()
    Float::class -> this.toFloat()
    Double::class -> this.toDouble()
    Boolean::class -> this.toBoolean()
    Char::class -> this[0]
    Short::class -> this.toShort()
    ByteArray::class -> this.toByteArray()
    else -> this
}
