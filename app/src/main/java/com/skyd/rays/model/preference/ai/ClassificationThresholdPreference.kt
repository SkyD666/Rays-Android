package com.skyd.rays.model.preference.ai

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.floatPreferencesKey
import com.skyd.rays.ext.dataStore
import com.skyd.rays.ext.put
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object ClassificationThresholdPreference {
    private const val CLASSIFICATION_THRESHOLD_DIR = "classificationThreshold"
    const val default = 0.5f

    val key = floatPreferencesKey(CLASSIFICATION_THRESHOLD_DIR)

    fun put(context: Context, scope: CoroutineScope, value: Float) {
        scope.launch(Dispatchers.IO) {
            context.dataStore.put(key, value)
        }
    }

    fun fromPreferences(preferences: Preferences): Float = preferences[key] ?: default
}