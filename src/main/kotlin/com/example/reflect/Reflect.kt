package com.example.reflect

import java.math.BigDecimal
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

inline fun <reified T : Any> getPropertyByAnnotation(annotation: KClass<out Annotation>) =
    T::class.memberProperties.find { it.annotations.any { it.annotationClass == annotation } }

inline fun String.parse(type: KClass<*>) = when (type) {
    Int::class -> this.toInt()
    Long::class -> this.toLong()
    Float::class -> this.toFloat()
    Double::class -> this.toDouble()
    Boolean::class -> this.toBoolean()
    BigDecimal::class -> this.toBigDecimal()
    Char::class -> this[0]
    Short::class -> this.toShort()
    ByteArray::class -> this.toByteArray()
    else -> this
}

inline fun <reified T : Any> getTypeByPropertyName(name: String) =
    T::class.memberProperties.find { it.name == name }?.returnType?.classifier

inline fun <reified T : Any> getTypeByAnnotation(annotation: KClass<out Annotation>) =
    getPropertyByAnnotation<T>(annotation)?.returnType?.classifier as KClass<*>

