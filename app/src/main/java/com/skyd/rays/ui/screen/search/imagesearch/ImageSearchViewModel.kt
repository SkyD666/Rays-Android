package com.skyd.rays.ui.screen.search.imagesearch

import androidx.lifecycle.viewModelScope
import com.skyd.rays.base.mvi.AbstractMviViewModel
import com.skyd.rays.ext.catchMap
import com.skyd.rays.ext.startWith
import com.skyd.rays.model.respository.ImageSearchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import javax.inject.Inject


@HiltViewModel
class ImageSearchViewModel @Inject constructor(private var selfieSegmentationRepo: ImageSearchRepository) :
    AbstractMviViewModel<ImageSearchIntent, ImageSearchState, ImageSearchEvent>() {

    override val viewState: StateFlow<ImageSearchState>

    init {
        val initialVS = ImageSearchState.initial()

        viewState = merge(
            intentSharedFlow.filterIsInstance<ImageSearchIntent.Init>().take(1),
            intentSharedFlow.filterNot { it is ImageSearchIntent.Init }
        )
            .shareWhileSubscribed()
            .toImageSearchPartialStateChangeFlow()
            .debugLog("ImageSearchPartialStateChange")
            .sendSingleEvent()
            .scan(initialVS) { vs, change -> change.reduce(vs) }
            .debugLog("ViewState")
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                initialVS
            )
    }

    private fun Flow<ImageSearchPartialStateChange>.sendSingleEvent(): Flow<ImageSearchPartialStateChange> {
        return onEach { change ->
            val event = when (change) {
                is ImageSearchPartialStateChange.ImageSearch.Failed ->
                    ImageSearchEvent.SearchUiEvent.Failed(change.msg)

                else -> return@onEach
            }
            sendEvent(event)
        }
    }

    private fun SharedFlow<ImageSearchIntent>.toImageSearchPartialStateChangeFlow()
            : Flow<ImageSearchPartialStateChange> {
        return merge(
            filterIsInstance<ImageSearchIntent.Init>().map {
                ImageSearchPartialStateChange.Init
            },
            filterIsInstance<ImageSearchIntent.Search>().flatMapConcat { intent ->
                selfieSegmentationRepo.imageSearch(
                    intent.base,
                    maxResultCount = intent.maxResultCount,
                ).map {
                    ImageSearchPartialStateChange.ImageSearch.Success(it)
                }.startWith(ImageSearchPartialStateChange.LoadingDialog)
                    .catchMap { ImageSearchPartialStateChange.ImageSearch.Failed(it.message.toString()) }
            },
        )
    }
}