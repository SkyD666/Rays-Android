package com.skyd.rays.ui.screen.settings.ml.classification.model

import com.skyd.rays.base.IUiState
import com.skyd.rays.model.bean.ModelBean

data class ClassificationModelState(
    val getModelsUiState: GetModelsUiState,
) : IUiState

sealed class GetModelsUiState {
    data object Init : GetModelsUiState()
    data class Success(val models: List<ModelBean>) : GetModelsUiState()
}
