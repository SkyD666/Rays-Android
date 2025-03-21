package com.skyd.rays.model.preference.theme

import androidx.datastore.preferences.core.stringPreferencesKey
import com.skyd.rays.model.preference.BasePreference

object CustomPrimaryColorPreference : BasePreference<String> {
    private const val CUSTOM_PRIMARY_COLOR = "customPrimaryColor"

    override val default = "62539F"
    override val key = stringPreferencesKey(CUSTOM_PRIMARY_COLOR)
}
