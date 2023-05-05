package com.skyd.rays.model.preference

import android.content.Context
import androidx.compose.ui.BiasAlignment
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.longPreferencesKey
import com.skyd.rays.ext.dataStore
import com.skyd.rays.ext.put
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object HomeShareButtonAlignmentPreference {
    private const val HOME_SHARE_BUTTON_ALIGNMENT = "homeShareButtonAlignment"
    val default = BiasAlignment(1f, -1f)

    // 两个 Float 占 64 位，使用一个 64 位的 Long 来存储
    // 高 32 位是 horizontalBias，低 32 位是 verticalBias
    val key = longPreferencesKey(HOME_SHARE_BUTTON_ALIGNMENT)

    fun put(context: Context, scope: CoroutineScope, value: BiasAlignment) {
        scope.launch(Dispatchers.IO) {
            val v = ((value.horizontalBias.toRawBits() + 0L) shl 32) or
                    ((value.verticalBias.toRawBits() + 0L) and 0x00000000FFFFFFFFL)
            context.dataStore.put(key, v)
        }
    }

    fun fromPreferences(preferences: Preferences): BiasAlignment {
        val l = preferences[key] ?: return default
        val horizontalBias = Float.fromBits((l ushr 32).toInt())
        val verticalBias = Float.fromBits((l and 0x00000000FFFFFFFFL).toInt())
        return BiasAlignment(horizontalBias = horizontalBias, verticalBias = verticalBias)
    }
}