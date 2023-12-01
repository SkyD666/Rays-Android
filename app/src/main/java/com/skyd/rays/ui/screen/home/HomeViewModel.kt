package com.skyd.rays.ui.screen.home

import androidx.lifecycle.viewModelScope
import com.skyd.rays.appContext
import com.skyd.rays.base.mvi.AbstractMviViewModel
import com.skyd.rays.ext.dataStore
import com.skyd.rays.ext.getOrDefault
import com.skyd.rays.ext.startWith
import com.skyd.rays.model.bean.StickerWithTags
import com.skyd.rays.model.preference.search.SearchResultReversePreference
import com.skyd.rays.model.preference.search.SearchResultSortPreference
import com.skyd.rays.model.respository.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val homeRepo: HomeRepository,
) : AbstractMviViewModel<HomeIntent, HomeState, HomeEvent>() {

    override val viewState: StateFlow<HomeState>

    init {
        val initialVS = HomeState.initial()

        viewState = merge(
            intentSharedFlow.filterIsInstance<HomeIntent.Initial>().take(1),
            intentSharedFlow.filterNot { it is HomeIntent.Initial }
        )
            .shareWhileSubscribed()
            .toPartialStateChangeFlow()
            .debugLog("PartialStateChange")
            .sendSingleEvent()
            .scan(initialVS) { vs, change -> change.reduce(vs) }
            .debugLog("ViewState")
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                initialVS
            )
    }

    private fun Flow<HomePartialStateChange>.sendSingleEvent(): Flow<HomePartialStateChange> {
        return onEach { change ->
            val event = when (change) {
                HomePartialStateChange.AddClickCount.Success -> HomeEvent.AddClickCount.Success
                is HomePartialStateChange.DeleteStickerWithTags.Success ->
                    HomeEvent.DeleteStickerWithTags.Success(change.stickerUuids)

                is HomePartialStateChange.ExportStickers.Success ->
                    HomeEvent.ExportStickers.Success(change.successCount)

                else -> return@onEach
            }
            sendEvent(event)
        }
    }

    private fun SharedFlow<HomeIntent>.toPartialStateChangeFlow(): Flow<HomePartialStateChange> {
        return merge(
            merge(
                filterIsInstance<HomeIntent.Initial>(),
                filterIsInstance<HomeIntent.RefreshHomeList>()
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

            filterIsInstance<HomeIntent.GetSearchBarPopularTagsList>().flatMapConcat {
                homeRepo.requestSearchBarPopularTags(count = 20)
                    .map { HomePartialStateChange.PopularTags.Success(it) }
                    .startWith(HomePartialStateChange.PopularTags.Loading)
            },

            filterIsInstance<HomeIntent.GetStickerWithTagsList>().flatMapConcat { intent ->
                homeRepo.requestStickerWithTagsList(keyword = intent.keyword)
                    .map { HomePartialStateChange.SearchResult.Success(sortSearchResultList(it)) }
                    .startWith(HomePartialStateChange.SearchResult.Loading)
            },

            filterIsInstance<HomeIntent.ExportStickers>().flatMapConcat { intent ->
                homeRepo.requestExportStickers(stickerUuids = intent.stickerUuids)
                    .map { HomePartialStateChange.ExportStickers.Success(it) }
                    .startWith(HomePartialStateChange.LoadingDialog)
            },

            filterIsInstance<HomeIntent.DeleteStickerWithTags>().flatMapConcat { intent ->
                homeRepo.requestDeleteStickerWithTagsDetail(stickerUuids = intent.stickerUuids)
                    .map { HomePartialStateChange.DeleteStickerWithTags.Success(it) }
                    .startWith(HomePartialStateChange.LoadingDialog)
            },

            merge(
                filterIsInstance<HomeIntent.ReverseStickerWithTagsList>(),
                filterIsInstance<HomeIntent.SortStickerWithTagsList>()
            ).flatMapConcat { intent ->
                val data = if (intent is HomeIntent.ReverseStickerWithTagsList) intent.data
                else (intent as HomeIntent.SortStickerWithTagsList).data
                flowOf(sortSearchResultList(data))
                    .flowOn(Dispatchers.IO)
                    .map { HomePartialStateChange.SearchResult.Success(it) }
                    .startWith(HomePartialStateChange.SearchResult.Loading)

            },

            filterIsInstance<HomeIntent.AddClickCount>()
                .flatMapConcat { homeRepo.requestAddClickCount(stickerUuid = it.stickerUuid) }
                .map { HomePartialStateChange.AddClickCount.Success },
        )
    }

    private fun sortSearchResultList(
        unsortedUnreversedData: List<StickerWithTags>,
        applyReverse: Boolean = true
    ): List<StickerWithTags> =
        when (appContext.dataStore.getOrDefault(SearchResultSortPreference)) {
            "CreateTime" -> unsortedUnreversedData.sortStickers(applyReverse) {
                it.sticker.createTime
            }

            "ModifyTime" -> unsortedUnreversedData.sortStickers(
                applyReverse,
                compareBy({ it.sticker.modifyTime }, { it.sticker.createTime })
            )

            "TagCount" -> unsortedUnreversedData.sortStickers(
                applyReverse,
                compareBy({ it.tags.size }, { it.sticker.createTime })
            )

            "Title" -> unsortedUnreversedData.sortStickers(
                applyReverse,
                compareBy({ it.sticker.title }, { it.sticker.createTime })
            )

            "ClickCount" -> unsortedUnreversedData.sortStickers(
                applyReverse,
                compareBy({ it.sticker.clickCount }, { it.sticker.createTime })
            )

            "ShareCount" -> unsortedUnreversedData.sortStickers(
                applyReverse,
                compareBy({ it.sticker.shareCount }, { it.sticker.createTime })
            )

            else -> unsortedUnreversedData.sortStickers(applyReverse) {
                it.sticker.createTime
            }
        }

    private fun <R : Comparable<R>> List<StickerWithTags>.sortStickers(
        applyReverse: Boolean = true,
        selector: (StickerWithTags) -> R?
    ): List<StickerWithTags> {
        return if (applyReverse && appContext.dataStore.getOrDefault(SearchResultReversePreference)) {
            sortedByDescending(selector)
        } else {
            sortedBy(selector)
        }
    }

    private fun List<StickerWithTags>.sortStickers(
        applyReverse: Boolean = true,
        comparator: Comparator<StickerWithTags>
    ): List<StickerWithTags> {
        return if (applyReverse && appContext.dataStore.getOrDefault(SearchResultReversePreference)) {
            sortedWith(comparator).reversed()
        } else {
            sortedWith(comparator)
        }
    }
}