package com.example.reflect

import java.math.BigDecimal
import kotlin.reflect.KClass
import kotlin.reflect.KProperty0
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

fun <TClass : Any> getPropertyByPropertyName(type: KClass<TClass>, name: String): KProperty0<*>? =
    type.memberProperties.find { it.name == name } as KProperty0<*>

inline fun <reified TClass : Any, reified TProperty : Any> getPropertyByPropertyName(name: String)
    : KProperty0<TProperty>? =
    TClass::class.memberProperties.find { it.name == name } as KProperty0<TProperty>

inline fun <reified TClass : Any, reified TProperty : Any> getTypeByPropertyName(name: String) =
    getPropertyByPropertyName<TClass, TProperty>(name)?.returnType?.classifier as KClass<*>

inline fun <reified T : Any> getTypeByAnnotation(annotation: KClass<out Annotation>) =
    getPropertyByAnnotation<T>(annotation)?.returnType?.classifier as KClass<*>

