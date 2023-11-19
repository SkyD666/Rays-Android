package com.skyd.rays.model.preference.ai

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.floatPreferencesKey
import com.skyd.rays.ext.dataStore
import com.skyd.rays.ext.put
import com.skyd.rays.model.preference.BasePreference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object TextRecognizeThresholdPreference : BasePreference<Float> {
    private const val TEXT_RECOGNIZE_THRESHOLD_DIR = "textRecognizeThreshold"
    override val default = 0.4f

    val key = floatPreferencesKey(TEXT_RECOGNIZE_THRESHOLD_DIR)

    fun put(context: Context, scope: CoroutineScope, value: Float) {
        scope.launch(Dispatchers.IO) {
            context.dataStore.put(key, value)
        }
    }

    override fun fromPreferences(preferences: Preferences): Float = preferences[key] ?: default
}