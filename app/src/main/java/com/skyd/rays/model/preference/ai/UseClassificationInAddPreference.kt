package com.skyd.rays.model.preference.ai

import androidx.datastore.preferences.core.booleanPreferencesKey
import com.skyd.rays.model.preference.BasePreference

object UseClassificationInAddPreference : BasePreference<Boolean> {
    private const val USE_CLASSIFICATION_IN_ADD = "useClassificationInAdd"

    override val default = true
    override val key = booleanPreferencesKey(USE_CLASSIFICATION_IN_ADD)
}