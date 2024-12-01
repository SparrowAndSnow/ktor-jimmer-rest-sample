package com.example.route

import com.example.validate.ValidationBuilder

interface ValidatorProvider<T> {
    var validator: (ValidationBuilder.(T) -> Unit)?
}

fun <T> ValidatorProvider<T>.validate(block: ValidationBuilder.(T) -> Unit) {
    validator = block
}
