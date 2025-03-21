package com.skyd.rays.model.preference.share

import androidx.datastore.preferences.core.booleanPreferencesKey
import com.skyd.rays.model.preference.BasePreference

object CopyStickerToClipboardWhenSharingPreference : BasePreference<Boolean> {
    private const val COPY_STICKER_TO_CLIPBOARD_WHEN_SHARING = "copyStickerToClipboardWhenSharing"

    override val default = true
    override val key = booleanPreferencesKey(COPY_STICKER_TO_CLIPBOARD_WHEN_SHARING)
}