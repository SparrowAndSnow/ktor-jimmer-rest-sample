package com.example.route

import com.example.reflect.getTypeByAnnotation
import org.babyfish.jimmer.sql.Id
import kotlin.reflect.KClass


interface KeyProvider<T : Any> {
    var key: Any?

    class Impl<T : Any>(override var key: Any?) : KeyProvider<T>
}

inline fun <reified T : Any> KeyProvider<T>.key(key: Any) {
    this.key = key
}


inline fun <reified T : Any> entityId(): KClass<*> = getTypeByAnnotation<T>(Id::class)

