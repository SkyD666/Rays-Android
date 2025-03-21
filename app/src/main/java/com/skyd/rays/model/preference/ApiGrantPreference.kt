package com.skyd.rays.model.preference

import androidx.datastore.preferences.core.booleanPreferencesKey

object ApiGrantPreference : BasePreference<Boolean> {
    private const val API_GRANT = "apiGrant"

    override val default = false
    override val key = booleanPreferencesKey(API_GRANT)
}