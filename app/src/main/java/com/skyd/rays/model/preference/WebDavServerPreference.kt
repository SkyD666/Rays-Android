package com.skyd.rays.model.preference

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.skyd.rays.ext.dataStore
import com.skyd.rays.ext.put
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object WebDavServerPreference : BasePreference<String> {
    private const val WEB_DAV_SERVER = "webDavServer"
    override val default = ""

    val key = stringPreferencesKey(WEB_DAV_SERVER)

    fun put(context: Context, scope: CoroutineScope, value: String) {
        scope.launch(Dispatchers.IO) {
            context.dataStore.put(key, value)
        }
    }

    override fun fromPreferences(preferences: Preferences): String = preferences[key] ?: default
}