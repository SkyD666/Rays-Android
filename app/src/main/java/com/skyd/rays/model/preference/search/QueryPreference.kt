package com.skyd.rays.model.preference.search

import androidx.datastore.preferences.core.stringPreferencesKey
import com.skyd.rays.model.preference.BasePreference

object QueryPreference : BasePreference<String> {
    private const val QUERY = "query"

    override val default = ""
    override val key = stringPreferencesKey(QUERY)
}
