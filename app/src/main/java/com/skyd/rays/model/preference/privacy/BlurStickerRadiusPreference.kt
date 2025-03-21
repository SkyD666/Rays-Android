package com.skyd.rays.model.preference.privacy

import androidx.datastore.preferences.core.floatPreferencesKey
import com.skyd.rays.model.preference.BasePreference

object BlurStickerRadiusPreference : BasePreference<Float> {
    private const val BLUR_STICKER_RADIUS = "blurStickerRadius"

    override val default = 25f
    override val key = floatPreferencesKey(BLUR_STICKER_RADIUS)
}