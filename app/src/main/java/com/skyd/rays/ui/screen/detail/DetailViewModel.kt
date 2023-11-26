package com.skyd.rays.ui.screen.detail

import androidx.lifecycle.viewModelScope
import com.skyd.rays.appContext
import com.skyd.rays.base.BaseViewModel
import com.skyd.rays.base.IUIChange
import com.skyd.rays.config.refreshStickerData
import com.skyd.rays.model.preference.CurrentStickerUuidPreference
import com.skyd.rays.model.respository.DetailRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onCompletion
import javax.inject.Inject


@HiltViewModel
class DetailViewModel @Inject constructor(private var detailRepo: DetailRepository) :
    BaseViewModel<DetailState, DetailEvent, DetailIntent>() {
    override fun initUiState(): DetailState = DetailState(StickerDetailUiState.Empty)

    override fun IUIChange.checkStateOrEvent() = this as? DetailState? to this as? DetailEvent

    override fun Flow<DetailIntent>.handleIntent(): Flow<IUIChange> = merge(
        doIsInstance<DetailIntent.GetStickerDetails> { intent ->
            if (intent.stickerUuid.isBlank()) {
                flow {
                    CurrentStickerUuidPreference.put(
                        context = appContext,
                        scope = viewModelScope,
                        value = CurrentStickerUuidPreference.default,
                    )
                    emit(uiStateFlow.value.copy(stickerDetailUiState = StickerDetailUiState.Empty))
                }.defaultFinally()
            } else {
                detailRepo.requestStickerWithTagsDetail(intent.stickerUuid)
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

        doIsInstance<DetailIntent.DeleteStickerWithTags> { intent ->
            detailRepo.requestDeleteStickerWithTagsDetail(intent.stickerUuids)
                .mapToUIChange {
                    copy(stickerDetailUiState = StickerDetailUiState.Empty)
                }
                .defaultFinally()
                .onCompletion {
                    refreshStickerData.tryEmit(Unit)
                }
        },

        doIsInstance<DetailIntent.ExportStickers> { intent ->
            detailRepo.requestExportStickers(intent.stickerUuids)
                .mapToUIChange { data ->
                    DetailEvent(detailResultUiEvent = DetailResultUiEvent.Success(data))
                }
                .defaultFinally()
        },
    )
}