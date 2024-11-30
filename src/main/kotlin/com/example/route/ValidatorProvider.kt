package com.example.route

import com.example.reflect.getTypeByAnnotation
import com.example.validate.ValidationBuilder
import org.babyfish.jimmer.sql.Id
import kotlin.reflect.KClass

interface ValidatorProvider<T> {
    var validator: (ValidationBuilder.(T) -> Unit)?


    class Impl<T>() : ValidatorProvider<T> {
        override var validator: (ValidationBuilder.(T) -> Unit)? = null
    }
}

fun <T> ValidatorProvider<T>.validate(block: ValidationBuilder.(T) -> Unit) {
    validator = block
}
