package com.skyd.rays.ui.screen.add

import android.util.Log
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

    sealed interface Init : AddPartialStateChange {
        data class Success(
            val stickerWithTags: StickerWithTags?,
            val waitingList: List<UriWithStickerUuidBean>,
            val suggestTags: Set<String>,
            val similarStickers: List<StickerWithTags>,
        ) : Init {
            override fun reduce(oldState: AddState) = oldState.copy(
                waitingList = waitingList,
                getStickersWithTagsState = if (stickerWithTags == null) {
                    GetStickersWithTagsState.Init
                } else GetStickersWithTagsState.Success(stickerWithTags),
                suggestTags = suggestTags.toList(),
                addedTags = stickerWithTags?.tags?.map { it.tag }.orEmpty(),
                addToAllTags = emptyList(),
                titleText = stickerWithTags?.sticker?.title.orEmpty(),
                similarStickers = similarStickers,
                loadingDialog = false,
            )
        }

        data class Failed(val msg: String) : Init {
            override fun reduce(oldState: AddState): AddState = oldState
        }
    }

    data class UpdateTitleText(val title: String) : AddPartialStateChange {
        override fun reduce(oldState: AddState) = oldState.copy(titleText = title)
    }

    data class UpdateCurrentTagText(val currentTag: String) : AddPartialStateChange {
        override fun reduce(oldState: AddState) = oldState.copy(currentTagText = currentTag)
    }

    data class ReplaceWaitingListSingleSticker(
        val sticker: UriWithStickerUuidBean,
        val index: Int,
        val getStickersWithTagsState: (oldState: AddState) -> GetStickersWithTagsState = { it.getStickersWithTagsState },
        val suggestTags: List<String>? = null,
        val similarStickers: List<StickerWithTags>? = null,
        val currentStickerChanged: Boolean = false,
    ) : AddPartialStateChange {
        override fun reduce(oldState: AddState): AddState {
            val getStickersWithTagsState = getStickersWithTagsState(oldState)
            val stickerWithTags =
                (getStickersWithTagsState as? GetStickersWithTagsState.Success)?.stickerWithTags
            return oldState.copy(
                waitingList = oldState.waitingList.toMutableList().apply { set(index, sticker) },
                getStickersWithTagsState = getStickersWithTagsState,
                suggestTags = suggestTags ?: oldState.suggestTags,
                addedTags = if (currentStickerChanged) {
                    stickerWithTags?.tags?.map { it.tag }.orEmpty()
                } else oldState.addedTags,
                titleText = if (currentStickerChanged) {
                    stickerWithTags?.sticker?.title.orEmpty()
                } else oldState.titleText,
                similarStickers = similarStickers ?: oldState.similarStickers,
            )
        }
    }

    data class RemoveWaitingListSingleSticker(
        val willRemove: UriWithStickerUuidBean,
        val getStickersWithTagsState: (oldState: AddState) -> GetStickersWithTagsState = { it.getStickersWithTagsState },
        val suggestTags: List<String>? = null,
        val similarStickers: List<StickerWithTags>? = null,
        val currentStickerChanged: Boolean = false,
    ) : AddPartialStateChange {
        override fun reduce(oldState: AddState): AddState {
            val getStickersWithTagsState = getStickersWithTagsState(oldState)
            val stickerWithTags =
                (getStickersWithTagsState as? GetStickersWithTagsState.Success)?.stickerWithTags
            Log.w("RemoveWaitingListSingleSticker reduce", "Fuck $this")
            return oldState.copy(
                waitingList = oldState.waitingList.toMutableList()
                    .apply { remove(willRemove) },
                getStickersWithTagsState = getStickersWithTagsState,
                suggestTags = suggestTags ?: oldState.suggestTags,
                addedTags = if (currentStickerChanged) {
                    stickerWithTags?.tags?.map { it.tag }.orEmpty()
                } else oldState.addedTags,
                titleText = if (currentStickerChanged) {
                    stickerWithTags?.sticker?.title.orEmpty()
                } else oldState.titleText,
                similarStickers = similarStickers ?: oldState.similarStickers,
            )
        }
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

    data class AddToWaitingList(
        val stickers: List<UriWithStickerUuidBean>,
        val getStickersWithTagsState: (oldState: AddState) -> GetStickersWithTagsState = { it.getStickersWithTagsState },
        val suggestTags: List<String>? = null,
        val similarStickers: List<StickerWithTags>? = null,
        val currentStickerChanged: Boolean = false,
    ) : AddPartialStateChange {
        override fun reduce(oldState: AddState): AddState {
            val getStickersWithTagsState = getStickersWithTagsState(oldState)
            val stickerWithTags =
                (getStickersWithTagsState as? GetStickersWithTagsState.Success)?.stickerWithTags
            return oldState.copy(
                waitingList = oldState.waitingList + stickers,
                getStickersWithTagsState = getStickersWithTagsState,
                suggestTags = suggestTags ?: oldState.suggestTags,
                addedTags = if (currentStickerChanged) {
                    stickerWithTags?.tags?.map { it.tag }.orEmpty()
                } else oldState.addedTags,
                titleText = if (currentStickerChanged) {
                    stickerWithTags?.sticker?.title.orEmpty()
                } else oldState.titleText,
                similarStickers = similarStickers ?: oldState.similarStickers,
            )
        }
    }

    sealed interface AllToAllTag : AddPartialStateChange {
        data class Add(val text: String) : AllToAllTag {
            override fun reduce(oldState: AddState): AddState = oldState.copy(
                addToAllTags = (oldState.addToAllTags + text).distinct(),
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
                    addedTags = (stickerWithTags.tags.map { it.tag } + oldState.addToAllTags).distinct(),
                    loadingDialog = false,
                )

                is Failed -> oldState
            }
        }

        data class Success(val stickerWithTags: StickerWithTags) : GetStickersWithTags
        data class Failed(val stickerUuid: String) : GetStickersWithTags
    }

    sealed interface RemoveSuggestTag : AddPartialStateChange {
        data class Success(val text: String) : RemoveSuggestTag {
            override fun reduce(oldState: AddState): AddState = oldState.copy(
                suggestTags = oldState.suggestTags - text
            )
        }

        data class Failed(val msg: String) : RemoveSuggestTag {
            override fun reduce(oldState: AddState): AddState = oldState
        }
    }

    sealed interface AddStickers : AddPartialStateChange {
        data class Duplicate(val stickerWithTags: StickerWithTags) : AddStickers {
            override fun reduce(oldState: AddState): AddState = oldState.copy(
                getStickersWithTagsState = GetStickersWithTagsState.Success(stickerWithTags),
                addedTags = (stickerWithTags.tags.map { it.tag } + oldState.addToAllTags).distinct(),
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
