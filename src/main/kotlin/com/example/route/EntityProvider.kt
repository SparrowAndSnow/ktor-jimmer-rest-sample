package com.example.route

interface EntityProvider<T : Any> {
    var entity: T?
}

infix fun <T : Any> EntityProvider<T>.entity(block: () -> T) {
    entity = block()
}
