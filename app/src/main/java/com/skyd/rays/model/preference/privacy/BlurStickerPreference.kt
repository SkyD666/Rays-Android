package com.skyd.rays.model.preference.privacy

import androidx.datastore.preferences.core.booleanPreferencesKey
import com.skyd.rays.model.preference.BasePreference

object BlurStickerPreference : BasePreference<Boolean> {
    private const val BLUR_STICKER = "blurSticker"

    override val default = false
    override val key = booleanPreferencesKey(BLUR_STICKER)
}