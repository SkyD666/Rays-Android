package com.skyd.rays.model.preference

import androidx.compose.ui.layout.ContentScale
import androidx.datastore.preferences.core.stringPreferencesKey
import com.skyd.rays.R
import com.skyd.rays.appContext
import com.skyd.rays.ext.toDisplayName

object StickerScalePreference : BasePreference<String> {
    private const val STICKER_SCALE = "stickerScale"

    override val default = "FillWidth"
    override val key = stringPreferencesKey(STICKER_SCALE)

    val scaleList = arrayOf(
        "Crop",
        "Inside",
        "FillWidth",
        "FillHeight",
        "Fit",
        "FillBounds",
    )

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