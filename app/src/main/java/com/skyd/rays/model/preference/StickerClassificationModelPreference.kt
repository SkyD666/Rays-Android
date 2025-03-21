package com.skyd.rays.model.preference

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import com.skyd.rays.ext.dataStore
import com.skyd.rays.ext.put

object StickerClassificationModelPreference : BasePreference<String> {
    private const val STICKER_CLASSIFICATION_MODEL = "stickerClassificationModel"

    override val default = ""
    override val key = stringPreferencesKey(STICKER_CLASSIFICATION_MODEL)

    suspend fun put(context: Context, value: String) {
        context.dataStore.put(key, value)
    }
}