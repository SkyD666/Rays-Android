package com.skyd.rays.model.preference

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.skyd.rays.ext.dataStore
import com.skyd.rays.ext.put
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object StickerClassificationModelPreference {
    private const val STICKER_CLASSIFICATION_MODEL = "stickerClassificationModel"
    const val default = ""

    val key = stringPreferencesKey(STICKER_CLASSIFICATION_MODEL)

    fun put(context: Context, scope: CoroutineScope, value: String) {
        scope.launch(Dispatchers.IO) {
            context.dataStore.put(key, value)
        }
    }

    suspend fun put(context: Context, value: String) {
        context.dataStore.put(key, value)
    }

    fun fromPreferences(preferences: Preferences): String = preferences[key] ?: default
}