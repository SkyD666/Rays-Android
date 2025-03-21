package com.skyd.rays.model.preference

import androidx.datastore.preferences.core.stringPreferencesKey

object ExportStickerDirPreference : BasePreference<String> {
    private const val EXPORT_STICKER_DIR = "exportStickerDir"

    override val default = ""
    override val key = stringPreferencesKey(EXPORT_STICKER_DIR)
}