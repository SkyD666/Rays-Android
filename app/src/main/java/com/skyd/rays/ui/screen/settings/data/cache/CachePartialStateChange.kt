package com.skyd.rays.ui.screen.settings.data.cache

internal sealed interface CachePartialStateChange {
    fun reduce(oldState: CacheState): CacheState

    data object LoadingDialog : CachePartialStateChange {
        override fun reduce(oldState: CacheState): CacheState = oldState.copy(loadingDialog = true)
    }

    data object Init : CachePartialStateChange {
        override fun reduce(oldState: CacheState) = oldState
    }

    sealed interface DeleteDocumentsProviderThumbnails : CachePartialStateChange {
        data class Success(val time: Long) : DeleteDocumentsProviderThumbnails {
            override fun reduce(oldState: CacheState): CacheState =
                oldState.copy(loadingDialog = false)
        }
    }

    sealed interface DeleteAllMimetypes : CachePartialStateChange {
        data class Success(val time: Long) : DeleteAllMimetypes {
            override fun reduce(oldState: CacheState): CacheState =
                oldState.copy(loadingDialog = false)
        }
    }
}
