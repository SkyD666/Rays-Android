package com.skyd.rays.ext

inline fun <T> MutableCollection<T>.addIfAny(data: T, predicate: (T) -> Boolean) {
    if (find { !predicate(it) } == null) {
        this += data
    }
}