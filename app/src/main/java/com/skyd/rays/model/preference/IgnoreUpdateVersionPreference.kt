package com.skyd.rays.model.preference

import androidx.datastore.preferences.core.longPreferencesKey

object IgnoreUpdateVersionPreference : BasePreference<Long> {
    private const val IGNORE_UPDATE_VERSION = "ignoreUpdateVersion"

    override val default = 0L
    override val key = longPreferencesKey(IGNORE_UPDATE_VERSION)
}