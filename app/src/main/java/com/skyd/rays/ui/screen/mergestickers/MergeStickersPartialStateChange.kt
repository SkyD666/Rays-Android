package com.skyd.rays.ui.screen.mergestickers

import com.skyd.rays.model.bean.StickerWithTags

internal sealed interface MergeStickersPartialStateChange {
    fun reduce(oldState: MergeStickersState): MergeStickersState

    data object LoadingDialog : MergeStickersPartialStateChange {
        override fun reduce(oldState: MergeStickersState) = oldState.copy(loadingDialog = true)
    }

    sealed interface Init : MergeStickersPartialStateChange {
        override fun reduce(oldState: MergeStickersState): MergeStickersState {
            return when (this) {
                is Success -> oldState.copy(
                    stickersState = StickersState.Success(stickersList),
                    selectedTags = stickersList.map { sticker ->
                        sticker.tags.map { it.tag }
                    }.flatten().distinct(),
                    loadingDialog = false,
                )

                is Failed -> oldState.copy(
                    stickersState = StickersState.Failed(msg),
                    loadingDialog = false,
                )
            }
        }

        data class Success(val stickersList: List<StickerWithTags>) : Init
        data class Failed(val msg: String) : Init
    }

    sealed interface Merge : MergeStickersPartialStateChange {
        override fun reduce(oldState: MergeStickersState): MergeStickersState =
            oldState.copy(loadingDialog = false)

        data object Success : Merge
        data class Failed(val msg: String) : Merge
    }

    data class RemoveSelectedTag(val tag: String) : MergeStickersPartialStateChange {
        override fun reduce(oldState: MergeStickersState) = oldState.copy(
            selectedTags = oldState.selectedTags.toMutableList().apply { remove(tag) }
        )
    }

    data class AddSelectedTag(val tag: String) : MergeStickersPartialStateChange {
        override fun reduce(oldState: MergeStickersState) = oldState.copy(
            selectedTags = oldState.selectedTags.toMutableList().apply {
                if (tag !in this) add(tag)
            }
        )
    }

    data class ReplaceAllSelectedTags(val tags: List<String>) : MergeStickersPartialStateChange {
        override fun reduce(oldState: MergeStickersState) = oldState.copy(
            selectedTags = tags
        )
    }
}
