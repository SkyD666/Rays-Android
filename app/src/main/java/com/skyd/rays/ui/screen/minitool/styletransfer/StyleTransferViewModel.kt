package com.skyd.rays.ui.screen.minitool.styletransfer

import com.skyd.rays.base.BaseViewModel
import com.skyd.rays.base.IUIChange
import com.skyd.rays.base.IUiEvent
import com.skyd.rays.ext.toBitmap
import com.skyd.rays.model.respository.StyleTransferRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.merge
import javax.inject.Inject


@HiltViewModel
class StyleTransferViewModel @Inject constructor(private var StyleTransferRepo: StyleTransferRepository) :
    BaseViewModel<StyleTransferState, IUiEvent, StyleTransferIntent>() {
    override fun initUiState(): StyleTransferState {
        return StyleTransferState(styleTransferResultUiState = StyleTransferResultUiState.Init)
    }

    override fun IUIChange.checkStateOrEvent() = this as? StyleTransferState to this as? IUiEvent

    override fun Flow<StyleTransferIntent>.handleIntent(): Flow<IUIChange> = merge(
        doIsInstance<StyleTransferIntent.Transfer> { intent ->
            StyleTransferRepo.requestTransferredImage(
                style = intent.style.toBitmap(),
                content = intent.content.toBitmap(),
            )
                .mapToUIChange { data ->
                    copy(styleTransferResultUiState = StyleTransferResultUiState.Success(data.first))
                }
                .defaultFinally()
        },
    )
}