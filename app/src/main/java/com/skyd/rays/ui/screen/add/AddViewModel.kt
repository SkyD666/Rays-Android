package com.skyd.rays.ui.screen.add

import androidx.lifecycle.viewModelScope
import com.skyd.rays.appContext
import com.skyd.rays.base.BaseViewModel
import com.skyd.rays.base.IUIChange
import com.skyd.rays.base.IUiState
import com.skyd.rays.model.preference.CurrentStickerUuidPreference
import com.skyd.rays.model.respository.AddRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.merge
import javax.inject.Inject

@HiltViewModel
class AddViewModel @Inject constructor(private var addRepository: AddRepository) :
    BaseViewModel<IUiState, AddEvent, AddIntent>() {
    override fun initUiState(): IUiState {
        return object : IUiState {}
    }

    override fun IUIChange.checkStateOrEvent() = this as? IUiState to this as? AddEvent

    override fun Flow<AddIntent>.handleIntent(): Flow<IUIChange> = merge(
        doIsInstance<AddIntent.GetStickerWithTags> { intent ->
            addRepository.requestGetStickerWithTags(intent.stickerUuid)
                .mapToUIChange { data ->
                    AddEvent(getStickersWithTagsUiEvent = GetStickersWithTagsUiEvent.Success(data))
                }
                .defaultFinally()
        },

        doIsInstance<AddIntent.AddNewStickerWithTags> { intent ->
            addRepository.requestAddStickerWithTags(intent.stickerWithTags, intent.stickerUri)
                .mapToUIChange(onError = { data ->
                    if (data.code == -2) {
                        AddEvent(
                            addStickersResultUiEvent = AddStickersResultUiEvent.Duplicate(data.data!!)
                        )
                    } else {
                        error(data.msg.toString())
                    }
                }) { data ->
                    CurrentStickerUuidPreference.put(
                        context = appContext,
                        scope = viewModelScope,
                        value = data
                    )
                    AddEvent(addStickersResultUiEvent = AddStickersResultUiEvent.Success(data))
                }
                .defaultFinally()
        },

        doIsInstance<AddIntent.GetSuggestTags> { intent ->
            addRepository.requestSuggestTags(intent.sticker)
                .mapToUIChange { data ->
                    AddEvent(recognizeTextUiEvent = RecognizeTextUiEvent.Success(data))
                }
                .defaultFinally()
        },
    )
}