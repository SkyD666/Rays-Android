package com.skyd.rays.ui.screen.settings.ml.classification.model

import com.skyd.rays.base.mvi.MviViewState
import com.skyd.rays.model.bean.ModelBean

data class ClassificationModelState(
    val getModelsState: GetModelsState,
    val loadingDialog: Boolean,
) : MviViewState {
    companion object {
        fun initial() = ClassificationModelState(
            getModelsState = GetModelsState.Init,
            loadingDialog = false,
        )
    }
}


sealed class GetModelsState {
    data object Init : GetModelsState()
    data class Success(val models: List<ModelBean>) : GetModelsState()
}
