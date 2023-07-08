package com.skyd.rays.ext

inline fun <T> MutableCollection<T>.addIfAny(data: T, predicate: (T) -> Boolean) {
    if (find { !predicate(it) } == null) {
        this += data
    }
}

fun <T> MutableCollection<T>.addAllDistinctly(newData: Collection<T>) {
    newData.forEach { item ->
        if (!contains(item)) {
            add(item)
        }
    }
}