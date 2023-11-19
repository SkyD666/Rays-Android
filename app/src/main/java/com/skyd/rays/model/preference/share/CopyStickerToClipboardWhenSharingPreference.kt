package com.skyd.rays.model.preference.share

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import com.skyd.rays.ext.dataStore
import com.skyd.rays.ext.put
import com.skyd.rays.model.preference.BasePreference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object CopyStickerToClipboardWhenSharingPreference : BasePreference<Boolean> {
    private const val COPY_STICKER_TO_CLIPBOARD_WHEN_SHARING = "copyStickerToClipboardWhenSharing"
    override val default = true

    val key = booleanPreferencesKey(COPY_STICKER_TO_CLIPBOARD_WHEN_SHARING)

    fun put(context: Context, scope: CoroutineScope, value: Boolean) {
        scope.launch(Dispatchers.IO) {
            context.dataStore.put(key, value)
        }
    }

    override fun fromPreferences(preferences: Preferences): Boolean = preferences[key] ?: default
}