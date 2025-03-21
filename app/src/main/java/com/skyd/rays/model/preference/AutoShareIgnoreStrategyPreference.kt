package com.skyd.rays.model.preference

import androidx.datastore.preferences.core.stringPreferencesKey

object AutoShareIgnoreStrategyPreference : BasePreference<String> {
    private const val AUTO_SHARE_IGNORE_STRATEGY = "autoShareIgnoreStrategy"

    override val default = ".*launcher.*"
    override val key = stringPreferencesKey(AUTO_SHARE_IGNORE_STRATEGY)
}