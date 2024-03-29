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

object IntersectSearchBySpacePreference : BasePreference<Boolean> {
    private const val INTERSECT_SEARCH_BY_SPACE = "intersectSearchBySpace"
    override val default = true

    val key = booleanPreferencesKey(INTERSECT_SEARCH_BY_SPACE)

    fun put(context: Context, scope: CoroutineScope, value: Boolean) {
        scope.launch(Dispatchers.IO) {
            context.dataStore.put(key, value)
        }
    }

    override fun fromPreferences(preferences: Preferences): Boolean = preferences[key] ?: default
}
