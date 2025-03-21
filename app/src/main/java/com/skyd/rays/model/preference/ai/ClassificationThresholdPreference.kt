package com.skyd.rays.model.preference.ai

import androidx.datastore.preferences.core.floatPreferencesKey
import com.skyd.rays.model.preference.BasePreference

object ClassificationThresholdPreference : BasePreference<Float> {
    private const val CLASSIFICATION_THRESHOLD_DIR = "classificationThreshold"

    override val default = 0.5f
    override val key = floatPreferencesKey(CLASSIFICATION_THRESHOLD_DIR)
}