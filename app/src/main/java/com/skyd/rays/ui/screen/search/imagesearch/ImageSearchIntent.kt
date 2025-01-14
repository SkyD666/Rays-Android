package com.skyd.rays.ui.screen.search.imagesearch

import android.net.Uri
import com.skyd.rays.base.mvi.MviIntent

sealed interface ImageSearchIntent : MviIntent {
    data object Init : ImageSearchIntent
    data class Search(val base: Uri, val maxResultCount: Int) : ImageSearchIntent
}