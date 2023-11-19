package com.skyd.rays.model.preference

import android.content.Context
import androidx.compose.ui.layout.ContentScale
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.skyd.rays.R
import com.skyd.rays.appContext
import com.skyd.rays.ext.dataStore
import com.skyd.rays.ext.put
import com.skyd.rays.ext.toDisplayName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object StickerScalePreference : BasePreference<String> {
    private const val STICKER_SCALE = "stickerScale"
    override val default = "FillWidth"

    val key = stringPreferencesKey(STICKER_SCALE)

    val scaleList = arrayOf(
        "Crop",
        "Inside",
        "FillWidth",
        "FillHeight",
        "Fit",
        "FillBounds",
    )

    fun put(context: Context, scope: CoroutineScope, value: String) {
        scope.launch(Dispatchers.IO) {
            context.dataStore.put(key, value)
        }
    }

    override fun fromPreferences(preferences: Preferences): String = preferences[key] ?: default

    fun toName(scale: ContentScale): String = when (scale) {
        ContentScale.Crop -> "Crop"
        ContentScale.Inside -> "Inside"
        ContentScale.FillWidth -> "FillWidth"
        ContentScale.FillHeight -> "FillHeight"
        ContentScale.Fit -> "Fit"
        ContentScale.FillBounds -> "FillBounds"
        else -> "FillWidth"
    }

    fun toContentScale(scale: String): ContentScale = when (scale) {
        "Crop" -> ContentScale.Crop
        "Inside" -> ContentScale.Inside
        "FillWidth" -> ContentScale.FillWidth
        "FillHeight" -> ContentScale.FillHeight
        "Fit" -> ContentScale.Fit
        "FillBounds" -> ContentScale.FillBounds
        else -> ContentScale.FillWidth
    }

    fun toDisplayName(scale: String): String = toContentScale(scale).toDisplayName().let {
        if (scale == default) appContext.getString(R.string.default_tag, it)
        else it
    }
}