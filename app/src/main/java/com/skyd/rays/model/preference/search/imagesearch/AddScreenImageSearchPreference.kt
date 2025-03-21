package com.skyd.rays.model.preference.search.imagesearch

import androidx.datastore.preferences.core.booleanPreferencesKey
import com.skyd.rays.model.preference.BasePreference

object AddScreenImageSearchPreference : BasePreference<Boolean> {
    private const val ADD_SCREEN_IMAGE_SEARCH = "addScreenImageSearch"

    override val default = false
    override val key = booleanPreferencesKey(ADD_SCREEN_IMAGE_SEARCH)
}