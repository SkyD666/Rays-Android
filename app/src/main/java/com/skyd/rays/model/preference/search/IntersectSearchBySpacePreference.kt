package com.skyd.rays.model.preference.search

import androidx.datastore.preferences.core.booleanPreferencesKey
import com.skyd.rays.model.preference.BasePreference

object IntersectSearchBySpacePreference : BasePreference<Boolean> {
    private const val INTERSECT_SEARCH_BY_SPACE = "intersectSearchBySpace"

    override val default = true
    override val key = booleanPreferencesKey(INTERSECT_SEARCH_BY_SPACE)
}
