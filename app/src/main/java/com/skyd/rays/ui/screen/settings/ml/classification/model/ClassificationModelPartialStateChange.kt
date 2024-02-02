package com.skyd.rays.ui.screen.settings.ml.classification.model

import android.net.Uri
import com.skyd.rays.model.bean.ModelBean

internal sealed interface ClassificationModelPartialStateChange {
    fun reduce(oldState: ClassificationModelState): ClassificationModelState

    data object LoadingDialog : ClassificationModelPartialStateChange {
        override fun reduce(oldState: ClassificationModelState) =
            oldState.copy(loadingDialog = true)
    }

    sealed interface GetModels : ClassificationModelPartialStateChange {
        override fun reduce(oldState: ClassificationModelState): ClassificationModelState {
            return when (this) {
                is Success -> oldState.copy(
                    getModelsState = GetModelsState.Success(models),
                    loadingDialog = false,
                )
            }
        }

        data class Success(val models: List<ModelBean>) : GetModels
    }

    sealed interface Import : ClassificationModelPartialStateChange {
        data class Success(val newModel: ModelBean) : Import {
            override fun reduce(oldState: ClassificationModelState) =
                if (oldState.getModelsState is GetModelsState.Success) {
                    oldState.copy(
                        getModelsState = GetModelsState.Success(
                            oldState.getModelsState.models.toMutableList() + newModel
                        ),
                        loadingDialog = false,
                    )
                } else oldState.copy(
                    loadingDialog = false,
                )
        }

        data class Failed(val msg: String) : Import {
            override fun reduce(oldState: ClassificationModelState) = oldState.copy(
                loadingDialog = false,
            )
        }
    }

    sealed interface SetModel : ClassificationModelPartialStateChange {
        data object Success : SetModel {
            override fun reduce(oldState: ClassificationModelState) = oldState.copy(
                loadingDialog = false,
            )
        }
    }

    sealed interface Delete : ClassificationModelPartialStateChange {
        data class Success(val deletedUri: Uri) : Delete {
            override fun reduce(oldState: ClassificationModelState) =
                if (oldState.getModelsState is GetModelsState.Success) {
                    oldState.copy(
                        getModelsState = GetModelsState.Success(
                            oldState.getModelsState.models.filter { it.uri != deletedUri }
                        ),
                        loadingDialog = false,
                    )
                } else oldState.copy(
                    loadingDialog = false,
                )
        }

        data class Failed(val msg: String) : Delete {
            override fun reduce(oldState: ClassificationModelState) = oldState.copy(
                loadingDialog = false,
            )
        }
    }
}
