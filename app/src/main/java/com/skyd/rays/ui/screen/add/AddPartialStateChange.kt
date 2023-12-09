package com.skyd.rays.ui.screen.add

import com.skyd.rays.model.bean.StickerWithTags
import com.skyd.rays.model.bean.UriWithStickerUuidBean

internal sealed interface AddPartialStateChange {
    fun reduce(oldState: AddState): AddState

    sealed interface LoadingDialog : AddPartialStateChange {
        data object Show : LoadingDialog {
            override fun reduce(oldState: AddState) = oldState.copy(loadingDialog = true)
        }

        data object Close : LoadingDialog {
            override fun reduce(oldState: AddState) = oldState.copy(loadingDialog = false)
        }
    }

    data object GetStickersWithTagsStateChanged : AddPartialStateChange {
        override fun reduce(oldState: AddState) = oldState
    }

    data class Init(
        val stickerWithTags: StickerWithTags?,
        val waitingList: List<UriWithStickerUuidBean>,
        val suggestTags: Set<String>,
    ) : AddPartialStateChange {
        override fun reduce(oldState: AddState) = oldState.copy(
            waitingList = waitingList,
            getStickersWithTagsState = if (stickerWithTags == null) {
                GetStickersWithTagsState.Init
            } else GetStickersWithTagsState.Success(stickerWithTags),
            suggestTags = suggestTags.toList(),
            addedTags = stickerWithTags?.tags?.map { it.tag }.orEmpty(),
            addToAllTags = emptyList(),
            loadingDialog = false,
        )
    }

    data object ProcessNext : AddPartialStateChange {
        override fun reduce(oldState: AddState) = oldState.copy(
            waitingList = oldState.waitingList.toMutableList().apply { removeFirstOrNull() },
            getStickersWithTagsState = GetStickersWithTagsState.Init,
            suggestTags = emptyList(),
            addedTags = oldState.addToAllTags,
            loadingDialog = false,
        )
    }

    data class ReplaceWaitingListFirst(val sticker: UriWithStickerUuidBean) :
        AddPartialStateChange {
        override fun reduce(oldState: AddState) = oldState.copy(
            waitingList = oldState.waitingList.toMutableList().apply { set(0, sticker) }
        )
    }

    data class AddTag(val tag: String) : AddPartialStateChange {
        override fun reduce(oldState: AddState) = oldState.copy(
            addedTags = (oldState.addedTags + tag).distinct(),
        )
    }

    data class RemoveTag(val tag: String) : AddPartialStateChange {
        override fun reduce(oldState: AddState) = oldState.copy(
            addedTags = oldState.addedTags - tag,
        )
    }

    data class AddToWaitingList(val stickers: List<UriWithStickerUuidBean>) :
        AddPartialStateChange {
        override fun reduce(oldState: AddState) = oldState.copy(
            waitingList = oldState.waitingList + stickers
        )
    }

    sealed interface AllToAllTag : AddPartialStateChange {
        data class Add(val text: String) : AllToAllTag {
            override fun reduce(oldState: AddState): AddState = oldState.copy(
                addToAllTags = (oldState.addToAllTags + text).distinct(),
                addedTags = (oldState.addedTags + text).distinct(),
                loadingDialog = false,
            )
        }

        data class Remove(val text: String) : AllToAllTag {
            override fun reduce(oldState: AddState): AddState = oldState.copy(
                addToAllTags = oldState.addToAllTags - text,
                loadingDialog = false,
            )
        }
    }

    sealed interface GetStickersWithTags : AddPartialStateChange {
        override fun reduce(oldState: AddState): AddState {
            return when (this) {
                is Success -> oldState.copy(
                    getStickersWithTagsState = GetStickersWithTagsState.Success(stickerWithTags),
                    addedTags = stickerWithTags.tags.map { it.tag },
                    loadingDialog = false,
                )

                is Failed -> oldState
            }
        }

        data class Success(val stickerWithTags: StickerWithTags) : GetStickersWithTags
        data class Failed(val stickerUuid: String) : GetStickersWithTags
    }

    sealed interface GetSuggestTags : AddPartialStateChange {

        data class Success(val texts: Set<String>) : GetSuggestTags {
            override fun reduce(oldState: AddState): AddState = oldState.copy(
                suggestTags = texts.toList(),
                loadingDialog = false,
            )
        }

        data class Remove(val text: String) : GetSuggestTags {
            override fun reduce(oldState: AddState): AddState = oldState.copy(
                suggestTags = oldState.suggestTags - text
            )
        }

        data class Failed(val msg: String) : GetSuggestTags {
            override fun reduce(oldState: AddState): AddState = oldState
        }
    }

    sealed interface AddStickers : AddPartialStateChange {
        data class Duplicate(val stickerWithTags: StickerWithTags) : AddStickers {
            override fun reduce(oldState: AddState): AddState = oldState.copy(
                getStickersWithTagsState = GetStickersWithTagsState.Success(stickerWithTags),
                addedTags = stickerWithTags.tags.map { it.tag },
                loadingDialog = false,
            )
        }

        data class Success(val stickerUuid: String) : AddStickers {
            override fun reduce(oldState: AddState): AddState = oldState.copy(
                loadingDialog = false,
            )
        }


        data class Failed(val msg: String) : AddStickers {
            override fun reduce(oldState: AddState): AddState = oldState.copy(
                loadingDialog = false,
            )
        }
    }
}
