package com.skyd.rays.model.preference.ai

import androidx.datastore.preferences.core.floatPreferencesKey
import com.skyd.rays.model.preference.BasePreference

object TextRecognizeThresholdPreference : BasePreference<Float> {
    private const val TEXT_RECOGNIZE_THRESHOLD_DIR = "textRecognizeThreshold"

    override val default = 0.4f
    override val key = floatPreferencesKey(TEXT_RECOGNIZE_THRESHOLD_DIR)
}