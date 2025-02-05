package com.skyd.rays.ui.screen.search

import com.skyd.rays.base.mvi.MviIntent

sealed interface SearchIntent : MviIntent {
    data object GetSearchData : SearchIntent
    data class AddSelectedStickers(val stickers: Collection<String>) : SearchIntent
    data class RemoveSelectedStickers(val stickers: Collection<String>) : SearchIntent
}