package com.skyd.rays.ui.screen.home

import android.util.Log
import com.skyd.rays.appContext
import com.skyd.rays.base.BaseData
import com.skyd.rays.base.BaseViewModel
import com.skyd.rays.base.IUIChange
import com.skyd.rays.base.PartialChange
import com.skyd.rays.base.ReqState
import com.skyd.rays.config.refreshStickerData
import com.skyd.rays.ext.dataStore
import com.skyd.rays.ext.getOrDefault
import com.skyd.rays.model.bean.StickerWithTags
import com.skyd.rays.model.bean.TagBean
import com.skyd.rays.model.preference.ShowPopularTagsPreference
import com.skyd.rays.model.preference.search.SearchResultReversePreference
import com.skyd.rays.model.preference.search.SearchResultSortPreference
import com.skyd.rays.model.respository.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject


@HiltViewModel
class HomeViewModel @Inject constructor(private var homeRepo: HomeRepository) :
    BaseViewModel<HomeState, HomeEvent, HomeIntent>() {
    override fun initUiState(): HomeState {
        return HomeState(
            HomeUiState.Init,
            SearchResultUiState.Init,
            PopularTagsUiState.Init,
        )
    }

    override fun IUIChange.checkStateOrEvent() = this as? HomeState? to this as? HomeEvent

    override fun Flow<HomeIntent>.handleIntent(): Flow<IUIChange> = merge(
        doIsInstance<HomeIntent.GetStickerWithTagsList> { intent ->
            homeRepo.requestStickerWithTagsList(intent.keyword)
                .mapToUIChange { data ->
                    copy(
                        searchResultUiState = SearchResultUiState.Success(
                            sortSearchResultList(data)
                        )
                    )
                }
                .defaultFinally()
        },

        doIsInstance<HomeIntent.GetHomeList> {
            homeRepo.requestRecommendTags()
                .combine(homeRepo.requestRandomTags()) { recommendTags, randomTags ->
                    recommendTags to randomTags
                }
                .combine(homeRepo.requestRecentCreateStickers()) { other, recentCreateStickers ->
                    Triple(other.first, other.second, recentCreateStickers)
                }.map {
                    BaseData<Triple<List<TagBean>, List<TagBean>, List<StickerWithTags>>>().apply {
                        state = ReqState.Success
                        data = it
                    }
                }
                .mapToUIChange { data ->
                    Log.e("TAG", "GetHomeList: ")
                    copy(
                        homeUiState = HomeUiState.Success(
                            recommendTagsList = data.first,
                            recentCreatedStickersList = data.third,
                            randomTagsList = data.second
                        )
                    )
                }.defaultFinally().onStart {
                    Log.e("TAG", "GetHomeList: onStart")
                }
        },

        doIsInstance<HomeIntent.DeleteStickerWithTags> { intent ->
            homeRepo.requestDeleteStickerWithTagsDetail(intent.stickerUuids)
                .mapToUIChange {
                    if (searchResultUiState is SearchResultUiState.Success) {
                        copy(
                            searchResultUiState = searchResultUiState.copy(
                                stickerWithTagsList = searchResultUiState.stickerWithTagsList
                                    .filter { !intent.stickerUuids.contains(it.sticker.uuid) }
                            )
                        )
                    } else {
                        this
                    }
                }
                .defaultFinally()
                .onCompletion {
                    refreshStickerData.tryEmit(Unit)
                }
        },

        doIsInstance<HomeIntent.SortStickerWithTagsList> { intent ->
            emptyFlow()
                .mapToUIChange {
                    copy(
                        searchResultUiState = SearchResultUiState.Success(
                            sortSearchResultList(intent.data)
                        )
                    )
                }
                .defaultFinally()
        },

        doIsInstance<HomeIntent.ReverseStickerWithTagsList> { intent ->
            emptyFlow()
                .mapToUIChange {
                    copy(
                        searchResultUiState = SearchResultUiState.Success(
                            sortSearchResultList(intent.data)
                        )
                    )
                }
                .defaultFinally()
        },

        doIsInstance<HomeIntent.AddClickCount> { intent ->
            homeRepo.requestAddClickCount(stickerUuid = intent.stickerUuid, count = intent.count)
                .mapToUIChange { this }
                .defaultFinally()
        },

        doIsInstance<HomeIntent.ExportStickers> { intent ->
            homeRepo.requestExportStickers(intent.stickerUuids)
                .mapToUIChange { data ->
                    HomeEvent(homeResultUiEvent = HomeResultUiEvent.Success(data))
                }
                .defaultFinally()
        },

        doIsInstance<HomeIntent.GetSearchBarPopularTagsList> {
            if (appContext.dataStore.getOrDefault(ShowPopularTagsPreference)) {
                homeRepo.requestSearchBarPopularTags(count = 15)
                    .mapToUIChange { data ->
                        copy(popularTagsUiState = PopularTagsUiState.Success(data))
                    }
            } else {
                emptyFlow()
                    .mapToUIChange { this }
                    .defaultFinally()
            }
        },
    )

    override fun Flow<HomeIntent>.handleIntent2(): Flow<PartialChange<HomeState>> = merge(
        doIsInstance2<HomeIntent.GetStickerWithTagsList> { intent ->
            homeRepo.requestStickerWithTagsList(intent.keyword)
                .mapToPartialChange { data ->
                    SearchResultUiState.Success(sortSearchResultList(data))
                }
                .defaultFinally()
        },

        doIsInstance2<HomeIntent.GetHomeList> {
            homeRepo.requestRecommendTags()
                .combine(homeRepo.requestRandomTags()) { recommendTags, randomTags ->
                    recommendTags to randomTags
                }
                .combine(homeRepo.requestRecentCreateStickers()) { other, recentCreateStickers ->
                    Triple(other.first, other.second, recentCreateStickers)
                }.map {
                    BaseData<Triple<List<TagBean>, List<TagBean>, List<StickerWithTags>>>().apply {
                        state = ReqState.Success
                        data = it
                    }
                }
                .mapToPartialChange { data ->
                    HomeUiState.Success(
                        recommendTagsList = data.first,
                        recentCreatedStickersList = data.third,
                        randomTagsList = data.second
                    )
                }.defaultFinally().onStart {
                    Log.e("TAG", "GetHomeList: onStart")
                }
        },

        doIsInstance2<HomeIntent.DeleteStickerWithTags> { intent ->
            homeRepo.requestDeleteStickerWithTagsDetail(intent.stickerUuids)
                .mapToPartialChange {
                    DeleteStickerWithTagsResultUiEvent.Success(intent.stickerUuids)
                }
                .defaultFinally()
                .onCompletion {
                    refreshStickerData.tryEmit(Unit)
                }
        },

        doIsInstance2<HomeIntent.SortStickerWithTagsList> { intent ->
            emptyFlow()
                .mapToPartialChange {
                    SearchResultUiState.Success(sortSearchResultList(intent.data))
                }
                .defaultFinally()
        },

        doIsInstance2<HomeIntent.ReverseStickerWithTagsList> { intent ->
            emptyFlow()
                .mapToPartialChange {
                    SearchResultUiState.Success(sortSearchResultList(intent.data))
                }
                .defaultFinally()
        },

        doIsInstance2<HomeIntent.AddClickCount> { intent ->
            homeRepo.requestAddClickCount(stickerUuid = intent.stickerUuid, count = intent.count)
                .mapToPartialChange { AddClickCountResultUiEvent.Success }
                .defaultFinally()
        },

        doIsInstance2<HomeIntent.ExportStickers> { intent ->
            homeRepo.requestExportStickers(intent.stickerUuids)
                .mapToPartialChange { data ->
                    HomeResultUiEvent.Success(data)
                }
                .defaultFinally()
        },

        doIsInstance2<HomeIntent.GetSearchBarPopularTagsList> {
            if (appContext.dataStore.getOrDefault(ShowPopularTagsPreference)) {
                homeRepo.requestSearchBarPopularTags(count = 15)
                    .mapToPartialChange { data ->
                        PopularTagsUiState.Success(data)
                    }
            } else {
                emptyFlow()
                    .mapToPartialChange { PopularTagsUiState.Init }
                    .defaultFinally()
            }
        },
    )

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