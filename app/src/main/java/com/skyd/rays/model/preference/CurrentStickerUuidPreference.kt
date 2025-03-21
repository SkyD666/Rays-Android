package com.skyd.rays.model.preference

import androidx.datastore.preferences.core.stringPreferencesKey

object CurrentStickerUuidPreference : BasePreference<String> {
    private const val CURRENT_STICKER_UUID = "currentStickerUuid"

    override val default = ""
    override val key = stringPreferencesKey(CURRENT_STICKER_UUID)
}