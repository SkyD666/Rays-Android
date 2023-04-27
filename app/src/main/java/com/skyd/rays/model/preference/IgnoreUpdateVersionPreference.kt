package com.skyd.rays.model.preference

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.longPreferencesKey
import com.skyd.rays.ext.dataStore
import com.skyd.rays.ext.put
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object IgnoreUpdateVersionPreference {
    private const val IGNORE_UPDATE_VERSION = "ignoreUpdateVersion"
    const val default = 0L

    val key = longPreferencesKey(IGNORE_UPDATE_VERSION)

    fun put(context: Context, scope: CoroutineScope, value: Long) {
        scope.launch(Dispatchers.IO) {
            context.dataStore.put(key, value)
        }
    }

    fun fromPreferences(preferences: Preferences): Long = preferences[key] ?: default
}