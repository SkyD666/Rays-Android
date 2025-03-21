package com.skyd.rays.model.preference.ai

import androidx.datastore.preferences.core.booleanPreferencesKey
import com.skyd.rays.model.preference.BasePreference

object UseTextRecognizeInAddPreference : BasePreference<Boolean> {
    private const val USE_TEXT_RECOGNIZE_IN_ADD = "useTextRecognizeInAdd"

    override val default = true
    override val key = booleanPreferencesKey(USE_TEXT_RECOGNIZE_IN_ADD)
}