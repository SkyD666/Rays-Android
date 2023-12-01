package com.skyd.rays.ui.screen.detail

import com.skyd.rays.model.bean.StickerWithTags

internal sealed interface DetailPartialStateChange {
    fun reduce(oldState: DetailState): DetailState

    sealed interface DetailInfo : DetailPartialStateChange {
        override fun reduce(oldState: DetailState): DetailState {
            return when (this) {
                is Success -> oldState.copy(
                    stickerDetailState = StickerDetailState.Success(stickerWithTags)
                )

                Loading -> oldState.copy(
                    stickerDetailState = oldState.stickerDetailState.apply { loading = true }
                )

                Empty -> oldState.copy(stickerDetailState = StickerDetailState.Init)
            }
        }

        data object Loading : DetailInfo
        data object Empty : DetailInfo      // 没从数据库获取到数据
        data class Success(val stickerWithTags: StickerWithTags) : DetailInfo
    }

    sealed interface Export : DetailPartialStateChange {
        override fun reduce(oldState: DetailState): DetailState = oldState

        data object Success : Export
        data object Failed : Export
    }

    sealed interface Delete : DetailPartialStateChange {
        data object Success : Delete {
            override fun reduce(oldState: DetailState): DetailState = oldState.copy(
                stickerDetailState = StickerDetailState.Init
            )
        }

        data object Failed : Delete {
            override fun reduce(oldState: DetailState): DetailState = oldState
        }
    }
}
