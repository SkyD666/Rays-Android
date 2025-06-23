package com.skyd.rays.ui.screen.minitool.styletransfer

import androidx.lifecycle.viewModelScope
import com.skyd.rays.base.mvi.AbstractMviViewModel
import com.skyd.rays.base.mvi.MviSingleEvent
import com.skyd.rays.ext.startWith
import com.skyd.rays.model.respository.StyleTransferRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take


class StyleTransferViewModel(
    private var styleTransferRepo: StyleTransferRepository
) : AbstractMviViewModel<StyleTransferIntent, StyleTransferState, MviSingleEvent>() {

    override val viewState: StateFlow<StyleTransferState>

    init {
        val initialVS = StyleTransferState.initial()

        viewState = merge(
            intentSharedFlow.filterIsInstance<StyleTransferIntent.Initial>().take(1),
            intentSharedFlow.filterNot { it is StyleTransferIntent.Initial }
        )
            .shareWhileSubscribed()
            .toStyleTransferPartialStateChangeFlow()
            .debugLog("StyleTransferPartialStateChange")
            .scan(initialVS) { vs, change -> change.reduce(vs) }
            .debugLog("ViewState")
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                initialVS
            )
    }

    private fun SharedFlow<StyleTransferIntent>.toStyleTransferPartialStateChangeFlow()
            : Flow<StyleTransferPartialStateChange> {
        return merge(
            filterIsInstance<StyleTransferIntent.Initial>()
                .map { StyleTransferPartialStateChange.Init },

            filterIsInstance<StyleTransferIntent.Transfer>().flatMapConcat { intent ->
                styleTransferRepo.requestTransferredImage(
                    style = intent.style,
                    content = intent.content
                )
                    .map { StyleTransferPartialStateChange.StyleTransfer.Success(it.first) }
                    .startWith(StyleTransferPartialStateChange.LoadingDialog)
            },
        )
    }
}