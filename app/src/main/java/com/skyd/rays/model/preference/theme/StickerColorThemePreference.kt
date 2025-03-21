package com.skyd.rays.model.preference.theme

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.skyd.rays.ext.dataStore
import com.skyd.rays.model.preference.BasePreference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object StickerColorThemePreference : BasePreference<Boolean> {
    private const val STICKER_COLOR_THEME = "stickerColorTheme"

    override val default = false
    override val key = booleanPreferencesKey(STICKER_COLOR_THEME)

    override fun put(context: Context, scope: CoroutineScope, value: Boolean) {
        scope.launch(Dispatchers.IO) {
            context.dataStore.edit { pref ->
                pref[key] = value
                if (value) pref[ThemeNamePreference.key] = ThemeNamePreference.CUSTOM_THEME_NAME
            }
        }
    }
}