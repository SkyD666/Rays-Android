package com.skyd.rays.model.preference.theme

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.skyd.rays.ext.dataStore
import com.skyd.rays.ext.put
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

object CustomPrimaryColorPreference {
    private const val CUSTOM_PRIMARY_COLOR = "customPrimaryColor"
    const val default = ""

    val key = stringPreferencesKey(CUSTOM_PRIMARY_COLOR)

    fun put(context: Context, scope: CoroutineScope, value: String) {
        scope.launch {
            context.dataStore.put(key, value)
        }
    }

    fun fromPreferences(preferences: Preferences) = preferences[key] ?: default
}
