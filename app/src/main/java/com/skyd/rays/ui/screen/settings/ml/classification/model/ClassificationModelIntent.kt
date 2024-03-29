package com.skyd.rays.ui.screen.settings.ml.classification.model

import android.net.Uri
import com.skyd.rays.base.mvi.MviIntent
import com.skyd.rays.model.bean.ModelBean

sealed interface ClassificationModelIntent : MviIntent {
    data class DeleteModel(val modelBean: ModelBean) : ClassificationModelIntent

    data class SetModel(val modelBean: ModelBean) : ClassificationModelIntent

    data class ImportModel(val uri: Uri) : ClassificationModelIntent

    data object GetModels : ClassificationModelIntent
}