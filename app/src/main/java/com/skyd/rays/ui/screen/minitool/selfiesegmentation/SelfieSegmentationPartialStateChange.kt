package com.skyd.rays.ui.screen.minitool.selfiesegmentation

import android.graphics.Bitmap

internal sealed interface SelfieSegmentationPartialStateChange {
    fun reduce(oldState: SelfieSegmentationState): SelfieSegmentationState

    data object LoadingDialog : SelfieSegmentationPartialStateChange {
        override fun reduce(oldState: SelfieSegmentationState) =
            oldState.copy(loadingDialog = true)
    }

    data object Init : SelfieSegmentationPartialStateChange {
        override fun reduce(oldState: SelfieSegmentationState) = oldState.copy(
            selfieSegmentationResultState = SelfieSegmentationResultState.Init,
            loadingDialog = false,
        )
    }

    sealed interface SelfieSegmentation : SelfieSegmentationPartialStateChange {
        data class Success(val image: Bitmap) : SelfieSegmentation {
            override fun reduce(oldState: SelfieSegmentationState): SelfieSegmentationState {
                return oldState.copy(
                    selfieSegmentationResultState = SelfieSegmentationResultState.Success(image = image),
                    loadingDialog = false,
                )
            }
        }

        data class Failed(val msg: String) : SelfieSegmentation {
            override fun reduce(oldState: SelfieSegmentationState): SelfieSegmentationState {
                return oldState.copy(
                    selfieSegmentationResultState = SelfieSegmentationResultState.Init,
                    loadingDialog = false,
                )
            }
        }
    }

    sealed interface Export : SelfieSegmentationPartialStateChange {
        data class Success(val bitmap: Bitmap) : Export {
            override fun reduce(oldState: SelfieSegmentationState) = oldState.copy(
                loadingDialog = false,
            )
        }
    }
}
