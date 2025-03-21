package com.skyd.rays.model.preference

import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.floatPreferencesKey

object StickerItemWidthPreference : BasePreference<Float> {
    private const val STICKER_ITEM_WIDTH = "stickerItemWidth"

    val range = 60.dp..200.dp

    override val default = 125f
    override val key = floatPreferencesKey(STICKER_ITEM_WIDTH)
}