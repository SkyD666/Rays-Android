package com.skyd.rays.model.preference

import android.content.Context
import androidx.compose.runtime.*
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.skyd.rays.ext.dataStore
import com.skyd.rays.ext.put
import com.skyd.rays.ui.local.LocalQuery
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object QueryPreference {
    private const val QUERY = "query"
    const val default = ""

    val key = stringPreferencesKey(QUERY)

    fun put(context: Context, scope: CoroutineScope, value: String) {
        scope.launch(Dispatchers.IO) {
            context.dataStore.put(key, value)
        }
    }

    fun fromPreferences(preferences: Preferences): String = preferences[key] ?: default
}

@Composable
fun rememberQuery(): MutableState<String> {
    val query = LocalQuery.current
    return remember(query) { mutableStateOf(query) }
}