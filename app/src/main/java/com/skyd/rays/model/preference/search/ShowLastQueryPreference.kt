package com.skyd.rays.model.preference.search

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import com.skyd.rays.ext.dataStore
import com.skyd.rays.ext.put
import com.skyd.rays.model.preference.BasePreference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object ShowLastQueryPreference : BasePreference<Boolean> {
    private const val SHOW_LAST_QUERY = "showLastQuery"
    override val default = false

    val key = booleanPreferencesKey(SHOW_LAST_QUERY)

    fun put(context: Context, scope: CoroutineScope, value: Boolean) {
        scope.launch(Dispatchers.IO) {
            context.dataStore.put(key, value)
        }
    }

    override fun fromPreferences(preferences: Preferences): Boolean = preferences[key] ?: default
}
