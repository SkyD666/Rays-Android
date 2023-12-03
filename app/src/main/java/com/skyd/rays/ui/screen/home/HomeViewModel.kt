package com.skyd.rays.ui.screen.home

import androidx.lifecycle.viewModelScope
import com.skyd.rays.base.mvi.AbstractMviViewModel
import com.skyd.rays.base.mvi.MviSingleEvent
import com.skyd.rays.ext.startWith
import com.skyd.rays.model.respository.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val homeRepo: HomeRepository,
) : AbstractMviViewModel<HomeIntent, HomeState, MviSingleEvent>() {

    override val viewState: StateFlow<HomeState>

    init {
        val initialVS = HomeState.initial()

        viewState = merge(
            intentSharedFlow.filterIsInstance<HomeIntent.GetHomeList>().take(1),
            intentSharedFlow.filterNot { it is HomeIntent.RefreshHomeList }
        )
            .shareWhileSubscribed()
            .toPartialStateChangeFlow()
            .debugLog("PartialStateChange")
            .scan(initialVS) { vs, change -> change.reduce(vs) }
            .debugLog("ViewState")
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                initialVS
            )
    }

    private fun SharedFlow<HomeIntent>.toPartialStateChangeFlow(): Flow<HomePartialStateChange> {
        return merge(
            merge(
                filterIsInstance<HomeIntent.GetHomeList>(),
                filterIsInstance<HomeIntent.RefreshHomeList>(),
            ).flatMapLatest {
                combine(
                    homeRepo.requestRecommendTags(),
                    homeRepo.requestRandomTags(),
                    homeRepo.requestRecentCreateStickers(),
                    homeRepo.requestMostSharedStickers(),
                ) { recommendTagsList, randomTagsList, recentCreatedStickersList, mostSharedStickersList ->
                    HomePartialStateChange.HomeList.Success(
                        recommendTagsList = recommendTagsList,
                        randomTagsList = randomTagsList,
                        recentCreatedStickersList = recentCreatedStickersList,
                        mostSharedStickersList = mostSharedStickersList,
                    )
                }.startWith(HomePartialStateChange.HomeList.Loading)
            },
        )
    }
}