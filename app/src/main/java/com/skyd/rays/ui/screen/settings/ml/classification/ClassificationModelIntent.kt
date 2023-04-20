package com.skyd.rays.ui.screen.settings.ml.classification

import android.net.Uri
import com.skyd.rays.base.IUiIntent

sealed class ClassificationModelIntent : IUiIntent {
    data class DeleteModel(val modelUri: Uri) : ClassificationModelIntent()

    data class SetModel(val modelUri: Uri) : ClassificationModelIntent()

    data class ImportModel(val modelUri: Uri) : ClassificationModelIntent()

    object GetModels : ClassificationModelIntent()
}