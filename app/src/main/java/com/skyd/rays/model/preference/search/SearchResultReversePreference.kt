package com.skyd.rays.model.preference.search

import androidx.datastore.preferences.core.booleanPreferencesKey
import com.skyd.rays.model.preference.BasePreference

object SearchResultReversePreference : BasePreference<Boolean> {
    private const val SEARCH_RESULT_REVERSE = "searchResultReverse"

    override val default = true
    override val key = booleanPreferencesKey(SEARCH_RESULT_REVERSE)
}
