package com.skyd.rays.model.preference.search.imagesearch

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import com.skyd.rays.ext.dataStore
import com.skyd.rays.ext.put
import com.skyd.rays.model.preference.BasePreference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object ImageSearchMaxResultCountPreference : BasePreference<Int> {
    private const val IMAGE_SEARCH_MAX_RESULT_COUNT = "imageSearchMaxResultCount"
    override val default = 20

    val key = intPreferencesKey(IMAGE_SEARCH_MAX_RESULT_COUNT)

    fun put(context: Context, scope: CoroutineScope, value: Int) {
        scope.launch(Dispatchers.IO) {
            context.dataStore.put(key, value)
        }
    }

    override fun fromPreferences(preferences: Preferences): Int = preferences[key] ?: default
}
