package com.skyd.rays.ui.screen.home

import androidx.lifecycle.viewModelScope
import com.skyd.rays.appContext
import com.skyd.rays.base.BaseViewModel
import com.skyd.rays.base.IUIChange
import com.skyd.rays.base.IUiEvent
import com.skyd.rays.config.refreshStickerData
import com.skyd.rays.ext.dataStore
import com.skyd.rays.ext.get
import com.skyd.rays.model.bean.StickerWithTags
import com.skyd.rays.model.preference.CurrentStickerUuidPreference
import com.skyd.rays.model.preference.search.SearchResultReversePreference
import com.skyd.rays.model.preference.search.SearchResultSortPreference
import com.skyd.rays.model.respository.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onCompletion
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(private var homeRepo: HomeRepository) :
    BaseViewModel<HomeState, IUiEvent, HomeIntent>() {
    override fun initUiState(): HomeState {
        return HomeState(
            StickerDetailUiState.Init(
                appContext.dataStore
                    .get(CurrentStickerUuidPreference.key) ?: CurrentStickerUuidPreference.default
            ),
            SearchResultUiState.Init,
        )
    }

    override fun IUIChange.checkStateOrEvent() = this as? HomeState? to this as? IUiEvent

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

        doIsInstance<HomeIntent.GetStickerDetails> { intent ->
            if (intent.stickerUuid.isBlank()) {
                flow {
                    CurrentStickerUuidPreference.put(
                        context = appContext,
                        scope = viewModelScope,
                        value = intent.stickerUuid
                    )
                    emit(uiStateFlow.value.copy(stickerDetailUiState = StickerDetailUiState.Init()))
                }.defaultFinally()
            } else {
                homeRepo.requestStickerWithTagsDetail(intent.stickerUuid)
                    .mapToUIChange { data ->
                        CurrentStickerUuidPreference.put(
                            context = appContext,
                            scope = viewModelScope,
                            value = data.sticker.uuid
                        )
                        copy(stickerDetailUiState = StickerDetailUiState.Success(data))
                    }
                    .defaultFinally()
            }
        },

        doIsInstance<HomeIntent.DeleteStickerWithTags> { intent ->
            homeRepo.requestDeleteStickerWithTagsDetail(intent.stickerUuid)
                .mapToUIChange {
                    CurrentStickerUuidPreference.put(
                        context = appContext,
                        scope = viewModelScope,
                        value = CurrentStickerUuidPreference.default
                    )
                    copy(stickerDetailUiState = StickerDetailUiState.Init())
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

        doIsInstance<HomeIntent.AddClickCountAndGetStickerDetails> { intent ->
            homeRepo.requestAddClickCount(stickerUuid = intent.stickerUuid, count = intent.count)
                .flatMapConcat {
                    homeRepo.requestStickerWithTagsDetail(stickerUuid = intent.stickerUuid)
                }
                .mapToUIChange { data ->
                    CurrentStickerUuidPreference.put(
                        context = appContext,
                        scope = viewModelScope,
                        value = intent.stickerUuid
                    )
                    copy(stickerDetailUiState = StickerDetailUiState.Success(data))
                }
                .defaultFinally()
        },
    )

    private fun sortSearchResultList(
        unsortedUnreversedData: List<StickerWithTags>,
        applyReverse: Boolean = true
    ): List<StickerWithTags> = when (appContext.dataStore.get(SearchResultSortPreference.key)) {
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
        return if (applyReverse &&
            appContext.dataStore.get(SearchResultReversePreference.key) == true
        ) {
            sortedByDescending(selector)
        } else {
            sortedBy(selector)
        }
    }

    private fun List<StickerWithTags>.sortStickers(
        applyReverse: Boolean = true,
        comparator: Comparator<StickerWithTags>
    ): List<StickerWithTags> {
        return if (applyReverse &&
            appContext.dataStore.get(SearchResultReversePreference.key) == true
        ) {
            sortedWith(comparator).reversed()
        } else {
            sortedWith(comparator)
        }
    }
}