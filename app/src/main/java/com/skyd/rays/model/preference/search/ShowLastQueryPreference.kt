package com.skyd.rays.model.preference.search

import androidx.datastore.preferences.core.booleanPreferencesKey
import com.skyd.rays.model.preference.BasePreference

object ShowLastQueryPreference : BasePreference<Boolean> {
    private const val SHOW_LAST_QUERY = "showLastQuery"

    override val default = false
    override val key = booleanPreferencesKey(SHOW_LAST_QUERY)
}
