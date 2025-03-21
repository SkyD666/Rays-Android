package com.skyd.rays.model.preference.share

import androidx.datastore.preferences.core.booleanPreferencesKey
import com.skyd.rays.model.preference.BasePreference

object UriStringSharePreference : BasePreference<Boolean> {
    private const val URI_STRING_SHARE = "uriStringShare"

    override val default = false
    override val key = booleanPreferencesKey(URI_STRING_SHARE)
}