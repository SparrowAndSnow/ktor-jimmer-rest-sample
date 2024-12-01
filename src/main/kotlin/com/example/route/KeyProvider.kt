package com.example.route


interface KeyProvider<T : Any> {
    var key: Any?
}

inline fun <reified T : Any> KeyProvider<T>.key(key: Any) {
    this.key = key
}




