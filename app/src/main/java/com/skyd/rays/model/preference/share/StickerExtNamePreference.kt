package com.skyd.rays.model.preference.share

import androidx.datastore.preferences.core.booleanPreferencesKey
import com.skyd.rays.model.preference.BasePreference

object StickerExtNamePreference : BasePreference<Boolean> {
    private const val STICKER_EXT_NAME = "stickerExtName"

    override val default = true
    override val key = booleanPreferencesKey(STICKER_EXT_NAME)
}