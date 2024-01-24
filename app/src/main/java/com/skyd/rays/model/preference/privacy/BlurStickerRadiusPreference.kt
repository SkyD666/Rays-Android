package com.skyd.rays.model.preference.privacy

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.floatPreferencesKey
import com.skyd.rays.ext.dataStore
import com.skyd.rays.ext.put
import com.skyd.rays.model.preference.BasePreference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object BlurStickerRadiusPreference : BasePreference<Float> {
    private const val BLUR_STICKER_RADIUS = "blurStickerRadius"
    override val default = 25f

    val key = floatPreferencesKey(BLUR_STICKER_RADIUS)

    fun put(context: Context, scope: CoroutineScope, value: Float) {
        scope.launch(Dispatchers.IO) {
            context.dataStore.put(key, value)
        }
    }

    override fun fromPreferences(preferences: Preferences): Float = preferences[key] ?: default
}