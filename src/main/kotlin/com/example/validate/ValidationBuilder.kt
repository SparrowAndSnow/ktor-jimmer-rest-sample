package com.example.validate

import org.babyfish.jimmer.kt.get
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaGetter


class ValidationBuilder(val entity: Any) {

    val violations = mutableListOf<String>()

    inline fun violation(block: () -> String) = violations.add(block())

    fun <T : Any> KProperty<T>.validate(
        message: String? = null,
        predicate: T.() -> Boolean,
    ): KProperty<T> = apply {
        val value = runCatching {
            val value = entity::class.memberProperties.find { it.name == name }.let {
                it?.isAccessible = true
                it?.getter?.call(entity)
            }
            value
        }.getOrElse {
            null
        } as T?
        if (value != null && !value.predicate()) {
            violation { message ?: "$jsonName is invalid" }
        }
    }


    fun KProperty<String>.notBlank(message: String? = null): KProperty<String> =
        validate(message ?: "$jsonName cannot be blank") { isNotBlank() }

//    fun KProperty0<String>.length(range: LongRange, message: String? = null): KProperty<String> =
//        validate(message ?: "$jsonName must be between ${range.first} and ${range.last} characters long") {
//            length in range
//        }
//
//    fun KProperty0<String>.length(max: Long, message: String? = null): KProperty<String> =
//        validate(message ?: "$jsonName must be between 0 and $max characters long") {
//            length in 0..max
//        }

//    fun KProperty<String>.validUrl(message: String? = null): KProperty<String> =
//        validate(message ?: "$jsonName must be a valid URL: https://example.com") { isValidUrl() }

//    fun KProperty<String>.validUUID(message: String? = null): KProperty<String> =
//        validate(message ?: "$jsonName must be a valid UUID: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx") {
//            toUUID().isSuccess
//        }

//    fun <T : Comparable<T>> KProperty0<T>.range(range: ClosedRange<T>, message: String? = null): KProperty<T> =
//        validate(message ?: "$jsonName must be between ${range.start} and ${range.endInclusive}") {
//            this in range
//        }
//
//    fun <T : Comparable<T>> KProperty0<T>.max(max: T, message: String? = null): KProperty<T> =
//        validate(message ?: "$jsonName must be less than $max") { this <= max }
//
//    fun <T : Comparable<T>> KProperty0<T>.min(min: T, message: String? = null): KProperty<T> =
//        validate(message ?: "$jsonName must be greater than $min") { this >= min }

//    fun <T : Temporal> KProperty<T>.before(before: T, message: String? = null): KProperty<T> =
//        validate(message ?: "$jsonName must be before $before") {
//            Duration.between(before, this).let { it.isPositive || it.isZero }
//        }
//
//    fun <T : Temporal> KProperty<T>.after(after: T, message: String? = null): KProperty<T> =
//        validate(message ?: "$jsonName must be after $after") {
//            Duration.between(after, this).let { it.isNegative || it.isZero }
//        }
//
//    fun <T : Temporal> KProperty<T>.between(from: T, to: T, message: String? = null): KProperty<T> =
//        validate(message ?: "$jsonName must be between from $from and to $to") {
//            Duration.between(from, this).let { it.isPositive || it.isZero }
//                && Duration.between(to, this).let { it.isNegative || it.isZero }
//        }
}

//private val Duration.isPositive
//    get() = !isNegative

/**
 * getter.findAnnotation<JsonProperty>()?.value // if you use jackson
 * getter.findAnnotation<SerialName>()?.value  // if you use kotlinx serialization
 */
private val KProperty<*>.jsonName
    get() = name

inline fun <T : Any> validateAll(entity: T, block: ValidationBuilder.(T) -> Unit): ValidationResult {
    val validationBuilder = ValidationBuilder(entity).apply { block(entity) }
    val violations = validationBuilder.violations
    return if (violations.isEmpty()) ValidationResult.Valid else ValidationResult.Invalid(violations)
}

sealed class ValidationResult {
    object Valid : ValidationResult()
    data class Invalid(val violations: List<String>) : ValidationResult()
}
