package com.skyd.rays.ui.screen.search.multiselect

internal sealed interface MultiSelectPartialStateChange {
    fun reduce(oldState: MultiSelectState): MultiSelectState

    data object LoadingDialog : MultiSelectPartialStateChange {
        override fun reduce(oldState: MultiSelectState) =
            oldState.copy(loadingDialog = true)
    }

    data object Init : MultiSelectPartialStateChange {
        override fun reduce(oldState: MultiSelectState) = oldState
    }

    sealed interface DeleteStickerWithTags : MultiSelectPartialStateChange {
        override fun reduce(oldState: MultiSelectState) = oldState.copy(
            loadingDialog = false,
        )

        data object Success : DeleteStickerWithTags
        data class Failed(val msg: String) : DeleteStickerWithTags
    }

    sealed interface SendStickers : MultiSelectPartialStateChange {
        override fun reduce(oldState: MultiSelectState) = oldState.copy(
            loadingDialog = false,
        )

        data object Success : SendStickers
    }

    sealed interface ExportStickers : MultiSelectPartialStateChange {
        class Success(val successCount: Int) : ExportStickers {
            override fun reduce(oldState: MultiSelectState) = oldState.copy(loadingDialog = false)
        }
    }
}
