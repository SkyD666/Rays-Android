package com.skyd.rays.model.preference.share

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import com.skyd.rays.ext.dataStore
import com.skyd.rays.ext.put
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object CopyStickerToClipboardPreference {
    private const val COPY_STICKER_TO_CLIPBOARD = "copyStickerToClipboard"
    const val default = true

    val key = booleanPreferencesKey(COPY_STICKER_TO_CLIPBOARD)

    fun put(context: Context, scope: CoroutineScope, value: Boolean) {
        scope.launch(Dispatchers.IO) {
            context.dataStore.put(key, value)
        }
    }

    fun fromPreferences(preferences: Preferences): Boolean = preferences[key] ?: default
}