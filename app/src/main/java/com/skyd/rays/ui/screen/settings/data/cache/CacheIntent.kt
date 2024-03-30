package com.skyd.rays.ui.screen.settings.data.cache

import com.skyd.rays.base.mvi.MviIntent

sealed interface CacheIntent : MviIntent {
    data object Init : CacheIntent
    data object DeleteDocumentsProviderThumbnails : CacheIntent
}