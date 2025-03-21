package com.skyd.rays.model.preference

import androidx.datastore.preferences.core.booleanPreferencesKey

object ShowPopularTagsPreference : BasePreference<Boolean> {
    private const val SHOW_POPULAR_TAGS = "showPopularTags"

    override val default = true
    override val key = booleanPreferencesKey(SHOW_POPULAR_TAGS)
}