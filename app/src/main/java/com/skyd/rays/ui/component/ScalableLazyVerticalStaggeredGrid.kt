package com.skyd.rays.ui.component

import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridScope
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.skyd.rays.ext.dataStore
import com.skyd.rays.ext.getOrDefault
import com.skyd.rays.ext.onScaleEvent
import com.skyd.rays.model.preference.StickerItemWidthPreference

@Composable
fun ScalableLazyVerticalStaggeredGrid(
    modifier: Modifier = Modifier,
    state: LazyStaggeredGridState = rememberLazyStaggeredGridState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    reverseLayout: Boolean = false,
    verticalItemSpacing: Dp = 0.dp,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(0.dp),
    flingBehavior: FlingBehavior = ScrollableDefaults.flingBehavior(),
    userScrollEnabled: Boolean = true,
    content: LazyStaggeredGridScope.() -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var stickerItemWidth by rememberSaveable {
        mutableFloatStateOf(context.dataStore.getOrDefault(StickerItemWidthPreference))
    }
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Adaptive(stickerItemWidth.dp),
        modifier = modifier.onScaleEvent(
            onFingerCountChange = { },
            onScale = {
                stickerItemWidth = (stickerItemWidth.dp * ((it - 1) * 0.9f + 1))
                    .coerceIn(StickerItemWidthPreference.range).value
                StickerItemWidthPreference.put(
                    context = context,
                    scope = scope,
                    value = stickerItemWidth,
                )
            }
        ),
        state = state,
        contentPadding = contentPadding,
        reverseLayout = reverseLayout,
        verticalItemSpacing = verticalItemSpacing,
        horizontalArrangement = horizontalArrangement,
        flingBehavior = flingBehavior,
        userScrollEnabled = userScrollEnabled,
        content = content,
    )
}