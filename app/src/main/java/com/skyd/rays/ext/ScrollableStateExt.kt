package com.skyd.rays.ext

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState

// 如果滚动到了最下面 或者 不够长不能滚动时为true
val ScrollState.inBottomOrNotLarge: Boolean
    get() = value < maxValue || maxValue == 0

// 如果滚动到了最下面 或者 不够长不能滚动时为true
val LazyListState.inBottomOrNotLarge: Boolean
    get() = canScrollForward || firstVisibleItemIndex == 0 && firstVisibleItemScrollOffset == 0