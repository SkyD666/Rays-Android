package com.skyd.rays.ui.screen.settings.ml.classification

import android.net.Uri
import com.skyd.rays.base.IUiState

data class ClassificationModelState(
    val getModelsUiState: GetModelsUiState,
) : IUiState

sealed class GetModelsUiState {
    object Init : GetModelsUiState()
    data class Success(val models: List<Uri>) : GetModelsUiState()
}
