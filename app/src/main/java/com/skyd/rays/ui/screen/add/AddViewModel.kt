package com.skyd.rays.ui.screen.add

import androidx.lifecycle.viewModelScope
import com.skyd.rays.appContext
import com.skyd.rays.base.BaseViewModel
import com.skyd.rays.base.IUIChange
import com.skyd.rays.model.preference.CurrentStickerUuidPreference
import com.skyd.rays.model.respository.AddRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.merge
import javax.inject.Inject

@HiltViewModel
class AddViewModel @Inject constructor(private var addRepository: AddRepository) :
    BaseViewModel<AddState, AddEvent, AddIntent>() {
    override fun initUiState(): AddState {
        return AddState(
            GetStickersWithTagsUiState.Init
        )
    }

    override fun IUIChange.checkStateOrEvent() = this as? AddState to this as? AddEvent

    override fun Flow<AddIntent>.handleIntent(): Flow<IUIChange> = merge(
        doIsInstance<AddIntent.GetStickerWithTags> { intent ->
            addRepository.requestGetStickerWithTags(intent.stickerUuid)
                .mapToUIChange { data ->
                    copy(getStickersWithTagsUiState = GetStickersWithTagsUiState.Success(data))
                }
                .defaultFinally()
        },

        doIsInstance<AddIntent.AddNewStickerWithTags> { intent ->
            addRepository.requestAddStickerWithTags(intent.stickerWithTags, intent.stickerUri)
                .mapToUIChange { data ->
                    CurrentStickerUuidPreference.put(
                        context = appContext,
                        scope = viewModelScope,
                        value = data
                    )
                    AddEvent(addStickersResultUiEvent = AddStickersResultUiEvent.Success(data))
                }
                .defaultFinally()
        }
    )
}