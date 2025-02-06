package com.skyd.rays.ext

import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList

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

suspend fun <T, R> List<T>.subList(step: Int, onEachSub: suspend (List<T>) -> R): List<R> {
    val result = mutableListOf<R>()
    for (i in indices step step) {
        result.add(onEachSub(this@subList.subList(fromIndex = i, toIndex = minOf(i + step, size))))
    }
    return result
}

suspend fun <T, R> List<T>.safeDbVariableNumber(onEachSub: suspend (List<T>) -> R): List<R> {
    return subList(900) { onEachSub(it) }
}

fun <T : Any> snapshotStateListSaver() = listSaver<SnapshotStateList<T>, T>(
    save = { stateList -> stateList.toList() },
    restore = { it.toMutableStateList() },
)