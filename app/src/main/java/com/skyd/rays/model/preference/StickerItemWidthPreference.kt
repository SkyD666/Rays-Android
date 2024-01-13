package com.skyd.rays.model.preference

import android.content.Context
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.floatPreferencesKey
import com.skyd.rays.ext.dataStore
import com.skyd.rays.ext.put
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object StickerItemWidthPreference : BasePreference<Float> {
    private const val STICKER_ITEM_WIDTH = "stickerItemWidth"
    override val default = 125f

    val range = 60.dp..200.dp

    val key = floatPreferencesKey(STICKER_ITEM_WIDTH)

    fun put(context: Context, scope: CoroutineScope, value: Float) {
        scope.launch(Dispatchers.IO) {
            context.dataStore.put(key, value)
        }
    }

    override fun fromPreferences(preferences: Preferences): Float = preferences[key] ?: default
}