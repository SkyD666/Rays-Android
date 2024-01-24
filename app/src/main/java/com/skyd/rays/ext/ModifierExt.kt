package com.skyd.rays.ext

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


/**
 * @param onFingerCountChange 屏幕手指数变化 原本是打算在缩放时屏蔽掉列表滚动和点击事件的
 * @param onScale 缩放
 */
fun Modifier.onScaleEvent(
    onFingerCountChange: ((Int) -> Unit)? = null,
    onScale: (Float) -> Unit
): Modifier = composed {
    var fingerCount by rememberSaveable { mutableIntStateOf(0) }

    pointerInput(Unit) {
        awaitEachGesture {
            do {
                val event = awaitPointerEvent()
                if (event.changes.size != fingerCount) {
                    fingerCount = event.changes.size
                    onFingerCountChange?.invoke(fingerCount)
                }
                if (event.changes.size >= 2) {
                    val z = event.calculateZoom()
                    if (z != 1f) {
                        onScale(z)
                        event.changes.forEach { it.consume() }
                    }
                }
            } while (event.changes.any { it.pressed })
            fingerCount = 0
            onFingerCountChange?.invoke(fingerCount)
        }
    }
}

fun Modifier.simpleVerticalScrollbar(
    state: LazyGridState,
    width: Dp = 4.dp
): Modifier = composed {
    val targetAlpha = if (state.isScrollInProgress) 1f else 0f
    val duration = if (state.isScrollInProgress) 150 else 500

    val alpha by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = tween(durationMillis = duration),
        label = "simpleVerticalScrollbarAlpha"
    )

    drawWithContent {
        drawContent()

        val firstVisibleElementIndex = state.layoutInfo.visibleItemsInfo.firstOrNull()?.index
        val needDrawScrollbar = state.isScrollInProgress || alpha > 0.0f

        // Draw scrollbar if scrolling or if the animation is still running and lazy column has content
        if (needDrawScrollbar && firstVisibleElementIndex != null) {
            val elementHeight = this.size.height / state.layoutInfo.totalItemsCount
//            val scrollbarOffsetY = firstVisibleElementIndex * elementHeight + state.firstVisibleItemScrollOffset / 4
//            val scrollbarHeight = elementHeight * 4
            val scrollbarOffsetY = firstVisibleElementIndex * elementHeight
            val scrollbarHeight = state.layoutInfo.visibleItemsInfo.size * elementHeight

            drawRect(
                color = Color(0x66000000),
                topLeft = Offset(this.size.width - width.toPx(), scrollbarOffsetY),
                size = Size(width.toPx(), scrollbarHeight),
                alpha = alpha
            )
        }
    }
}