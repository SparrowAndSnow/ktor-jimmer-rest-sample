package com.example.route


interface KeyProvider<T : Any> {
    var key: Any?

    class Impl<T : Any>(override var key: Any?) : KeyProvider<T>
}

inline fun <reified T : Any> KeyProvider<T>.key(key: Any) {
    this.key = key
}




