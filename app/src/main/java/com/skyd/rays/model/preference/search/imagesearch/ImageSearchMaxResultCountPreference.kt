package com.skyd.rays.model.preference.search.imagesearch

import androidx.datastore.preferences.core.intPreferencesKey
import com.skyd.rays.model.preference.BasePreference

object ImageSearchMaxResultCountPreference : BasePreference<Int> {
    private const val IMAGE_SEARCH_MAX_RESULT_COUNT = "imageSearchMaxResultCount"

    override val default = 20
    override val key = intPreferencesKey(IMAGE_SEARCH_MAX_RESULT_COUNT)
}
