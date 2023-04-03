package com.skyd.rays.ui.component.lazyverticalgrid.adapter

import android.content.res.Configuration
import com.skyd.rays.appContext
import com.skyd.rays.ext.screenIsLand
import com.skyd.rays.model.bean.StickerWithTags1
import com.skyd.rays.model.bean.MiniTool1Bean
import com.skyd.rays.model.bean.More1Bean

const val MAX_SPAN_SIZE = 60
fun animeShowSpan(
    data: Any,
    enableLandScape: Boolean = true,
    configuration: Configuration = appContext.resources.configuration
): Int = if (enableLandScape && configuration.screenIsLand) {
    when (data) {
        is StickerWithTags1 -> MAX_SPAN_SIZE / 3
        is More1Bean -> MAX_SPAN_SIZE / 3
        is MiniTool1Bean -> MAX_SPAN_SIZE / 2
        else -> MAX_SPAN_SIZE / 3
    }
} else {
    when (data) {
        is StickerWithTags1 -> MAX_SPAN_SIZE / 2
        is More1Bean -> MAX_SPAN_SIZE / 2
        is MiniTool1Bean -> MAX_SPAN_SIZE / 1
        else -> MAX_SPAN_SIZE / 1
    }
}