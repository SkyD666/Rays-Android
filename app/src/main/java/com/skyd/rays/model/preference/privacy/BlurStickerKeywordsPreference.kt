package com.skyd.rays.model.preference.privacy

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.skyd.rays.ext.dataStore
import com.skyd.rays.ext.getOrDefault
import com.skyd.rays.model.preference.BasePreference
import com.skyd.rays.ui.local.LocalBlurSticker
import com.skyd.rays.ui.local.LocalBlurStickerKeywords

object BlurStickerKeywordsPreference : BasePreference<Set<String>> {
    private const val BLUR_STICKER_KEYWORDS = "blurStickerKeywords"

    override val default = setOf<String>()
    override val key = stringSetPreferencesKey(BLUR_STICKER_KEYWORDS)

    fun containsInKeywords(c: Collection<String>, keywords: Set<String>): Boolean {
        keywords.forEach {
            c.forEach { item ->
                if (item.contains(it)) {
                    return true
                }
            }
        }
        return false
    }
}

@Composable
fun rememberShouldBlur(c: Collection<String>): Boolean {
    val blurSticker = LocalBlurSticker.current
    val blurStickerKeywords = LocalBlurStickerKeywords.current
    return rememberSaveable(c, blurSticker, blurStickerKeywords) {
        blurSticker && BlurStickerKeywordsPreference.containsInKeywords(
            c = c, keywords = blurStickerKeywords
        )
    }
}

fun shouldBlur(context: Context, c: Collection<String>): Boolean {
    val blurSticker = context.dataStore.getOrDefault(BlurStickerPreference)
    val blurStickerKeywords = context.dataStore.getOrDefault(BlurStickerKeywordsPreference)
    return blurSticker && BlurStickerKeywordsPreference.containsInKeywords(
        c = c, keywords = blurStickerKeywords
    )
}