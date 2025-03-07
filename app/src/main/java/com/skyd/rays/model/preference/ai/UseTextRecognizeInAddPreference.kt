package com.skyd.rays.model.preference.ai

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import com.skyd.rays.ext.dataStore
import com.skyd.rays.ext.put
import com.skyd.rays.model.preference.BasePreference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object UseTextRecognizeInAddPreference : BasePreference<Boolean> {
    private const val USE_TEXT_RECOGNIZE_IN_ADD = "useTextRecognizeInAdd"
    override val default = true

    val key = booleanPreferencesKey(USE_TEXT_RECOGNIZE_IN_ADD)

    fun put(context: Context, scope: CoroutineScope, value: Boolean) {
        scope.launch(Dispatchers.IO) {
            context.dataStore.put(key, value)
        }
    }

    override fun fromPreferences(preferences: Preferences): Boolean = preferences[key] ?: default
}