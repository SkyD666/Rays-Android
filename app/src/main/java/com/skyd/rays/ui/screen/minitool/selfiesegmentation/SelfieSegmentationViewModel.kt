package com.skyd.rays.ui.screen.minitool.selfiesegmentation

import androidx.lifecycle.viewModelScope
import com.skyd.rays.base.mvi.AbstractMviViewModel
import com.skyd.rays.ext.startWith
import com.skyd.rays.model.respository.SelfieSegmentationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import javax.inject.Inject


@HiltViewModel
class SelfieSegmentationViewModel @Inject constructor(private var selfieSegmentationRepo: SelfieSegmentationRepository) :
    AbstractMviViewModel<SelfieSegmentationIntent, SelfieSegmentationState, SelfieSegmentationEvent>() {

    override val viewState: StateFlow<SelfieSegmentationState>

    init {
        val initialVS = SelfieSegmentationState.initial()

        viewState = merge(
            intentSharedFlow.filterIsInstance<SelfieSegmentationIntent.Initial>().take(1),
            intentSharedFlow.filterNot { it is SelfieSegmentationIntent.Initial }
        )
            .shareWhileSubscribed()
            .toSelfieSegmentationPartialStateChangeFlow()
            .debugLog("SelfieSegmentationPartialStateChange")
            .sendSingleEvent()
            .scan(initialVS) { vs, change -> change.reduce(vs) }
            .debugLog("ViewState")
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                initialVS
            )
    }

    private fun Flow<SelfieSegmentationPartialStateChange>.sendSingleEvent(): Flow<SelfieSegmentationPartialStateChange> {
        return onEach { change ->
            val event = when (change) {
                is SelfieSegmentationPartialStateChange.Export.Success -> {
                    SelfieSegmentationEvent.ExportUiEvent.Success(change.bitmap)
                }

                else -> return@onEach
            }
            sendEvent(event)
        }
    }

    private fun SharedFlow<SelfieSegmentationIntent>.toSelfieSegmentationPartialStateChangeFlow()
            : Flow<SelfieSegmentationPartialStateChange> {
        return merge(
            filterIsInstance<SelfieSegmentationIntent.Initial>()
                .map { SelfieSegmentationPartialStateChange.Init },

            filterIsInstance<SelfieSegmentationIntent.Segment>().flatMapConcat { intent ->
                selfieSegmentationRepo.requestSelfieSegment(foregroundUri = intent.foregroundUri)
                    .map { SelfieSegmentationPartialStateChange.SelfieSegmentation.Success(it) }
                    .startWith(SelfieSegmentationPartialStateChange.LoadingDialog)
            },

            filterIsInstance<SelfieSegmentationIntent.Export>().flatMapConcat { intent ->
                selfieSegmentationRepo.requestExport(
                    foregroundBitmap = intent.foregroundBitmap,
                    backgroundUri = intent.backgroundUri,
                    backgroundSize = intent.backgroundSize,
                    foregroundScale = intent.foregroundScale,
                    foregroundOffset = intent.foregroundOffset,
                    foregroundRotation = intent.foregroundRotation,
                    foregroundSize = intent.foregroundSize,
                    borderSize = intent.borderSize,
                )
                    .map { SelfieSegmentationPartialStateChange.Export.Success(it) }
                    .startWith(SelfieSegmentationPartialStateChange.LoadingDialog)
            },
        )
    }
}