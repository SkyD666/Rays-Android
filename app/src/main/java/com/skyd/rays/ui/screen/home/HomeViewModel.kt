package com.skyd.rays.ui.screen.home

import androidx.lifecycle.viewModelScope
import com.skyd.rays.appContext
import com.skyd.rays.base.BaseViewModel
import com.skyd.rays.base.IUIChange
import com.skyd.rays.base.IUiEvent
import com.skyd.rays.config.refreshStickerData
import com.skyd.rays.ext.dataStore
import com.skyd.rays.ext.get
import com.skyd.rays.model.preference.CurrentStickerUuidPreference
import com.skyd.rays.model.respository.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
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
                    copy(searchResultUiState = SearchResultUiState.Success(data))
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
    )
}