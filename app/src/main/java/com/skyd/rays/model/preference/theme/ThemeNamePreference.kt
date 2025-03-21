package com.skyd.rays.model.preference.theme

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.skyd.rays.ext.dataStore
import com.skyd.rays.model.preference.BasePreference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object ThemeNamePreference : BasePreference<String> {
    private const val THEME_NAME = "themeName"
    const val CUSTOM_THEME_NAME = "Custom"

    val values: List<ThemeItem> = mutableListOf(
        ThemeItem(
            name = "Purple",
            keyColor = Color(0xFF62539F)
        ),
        ThemeItem(
            name = "Blue",
            keyColor = Color(0xFF80BBFF)
        ),
        ThemeItem(
            name = "Pink",
            keyColor = Color(0xFFFFD8E4)
        ),
        ThemeItem(
            name = "Yellow",
            keyColor = Color(0xFFE9B666)
        ),
        ThemeItem(
            name = "Green",
            keyColor = Color(0xFF4CAF50)
        ),
    )

    override val default = values[0].name
    override val key = stringPreferencesKey(THEME_NAME)

    override fun put(context: Context, scope: CoroutineScope, value: String) {
        scope.launch(Dispatchers.IO) {
            context.dataStore.edit { pref ->
                pref[key] = value
                pref[StickerColorThemePreference.key] = false
            }
        }
    }

    fun isCustom(v: String): Boolean = v == CUSTOM_THEME_NAME

    data class ThemeItem(val name: String, val keyColor: Color)
}
