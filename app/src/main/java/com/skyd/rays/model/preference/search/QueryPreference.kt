package com.skyd.rays.model.preference.search

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.skyd.rays.ext.dataStore
import com.skyd.rays.ext.put
import com.skyd.rays.model.preference.BasePreference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object QueryPreference : BasePreference<String> {
    private const val QUERY = "query"
    override val default = ""

    val key = stringPreferencesKey(QUERY)

    fun put(context: Context, scope: CoroutineScope, value: String) {
        scope.launch(Dispatchers.IO) {
            context.dataStore.put(key, value)
        }
    }

    override fun fromPreferences(preferences: Preferences): String = preferences[key] ?: default
}
