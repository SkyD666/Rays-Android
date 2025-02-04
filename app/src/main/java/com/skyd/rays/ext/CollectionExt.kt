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

fun <T> List<T>.subList(step: Int, onEachSub: (List<T>) -> Unit) {
    for (i in indices step step) {
        onEachSub(
            subList(
                fromIndex = i,
                toIndex = minOf(i + step, size),
            )
        )
    }
}

fun <T> List<T>.safeDbVariableNumber(onEachSub: (List<T>) -> Unit) {
    subList(900, onEachSub)
}

fun <T : Any> snapshotStateListSaver() = listSaver<SnapshotStateList<T>, T>(
    save = { stateList -> stateList.toList() },
    restore = { it.toMutableStateList() },
)