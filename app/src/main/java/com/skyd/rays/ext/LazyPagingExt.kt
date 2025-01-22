package com.skyd.rays.ext

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import kotlinx.coroutines.flow.Flow
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

@Composable
fun <T : Any> Flow<PagingData<T>>.collectAsLazyPagingItemsWithLifecycle(
    context: CoroutineContext = EmptyCoroutineContext
): LazyPagingItems<T> {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    return remember(this, lifecycle) {
        flowWithLifecycle(lifecycle)
    }.collectAsLazyPagingItems(context)
}