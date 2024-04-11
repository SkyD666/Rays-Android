package com.skyd.rays.model.preference.theme

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.skyd.rays.ext.dataStore
import com.skyd.rays.ext.put
import com.skyd.rays.model.preference.BasePreference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

object CustomPrimaryColorPreference : BasePreference<String> {
    private const val CUSTOM_PRIMARY_COLOR = "customPrimaryColor"
    override val default = "62539F"

    val key = stringPreferencesKey(CUSTOM_PRIMARY_COLOR)

    fun put(context: Context, scope: CoroutineScope, value: String) {
        scope.launch {
            context.dataStore.put(key, value)
        }
    }

    override fun fromPreferences(preferences: Preferences) = preferences[key] ?: default
}
