package com.example.route

import com.example.reflect.getTypeByAnnotation
import org.babyfish.jimmer.sql.Id
import kotlin.reflect.KClass

interface EntityProvider<T : Any> {
    var entity: T?
}

infix fun <T : Any> EntityProvider<T>.entity(block: () -> T) {
    entity = block()
}


inline fun <reified T : Any> entityIdType(): KClass<*> = getTypeByAnnotation<T>(Id::class)
