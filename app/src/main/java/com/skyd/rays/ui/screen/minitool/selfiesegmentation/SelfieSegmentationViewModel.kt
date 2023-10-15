package com.skyd.rays.ui.screen.minitool.selfiesegmentation

import com.skyd.rays.base.BaseViewModel
import com.skyd.rays.base.IUIChange
import com.skyd.rays.model.respository.SelfieSegmentationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.merge
import javax.inject.Inject


@HiltViewModel
class SelfieSegmentationViewModel @Inject constructor(private var selfieSegmentationRepo: SelfieSegmentationRepository) :
    BaseViewModel<SelfieSegmentationState, SelfieSegmentationEvent, SelfieSegmentationIntent>() {
    override fun initUiState(): SelfieSegmentationState {
        return SelfieSegmentationState(selfieSegmentationResultUiState = SelfieSegmentationResultUiState.Init)
    }

    override fun IUIChange.checkStateOrEvent() =
        this as? SelfieSegmentationState to this as? SelfieSegmentationEvent

    override fun Flow<SelfieSegmentationIntent>.handleIntent(): Flow<IUIChange> = merge(
        doIsInstance<SelfieSegmentationIntent.Segment> { intent ->
            selfieSegmentationRepo.requestSelfieSegment(foregroundUri = intent.foregroundUri)
                .mapToUIChange { data ->
                    copy(
                        selfieSegmentationResultUiState =
                        SelfieSegmentationResultUiState.Success(data.first)
                    )
                }.defaultFinally()
        },

        doIsInstance<SelfieSegmentationIntent.Export> { intent ->
            selfieSegmentationRepo.requestExport(
                foregroundBitmap = intent.foregroundBitmap,
                backgroundUri = intent.backgroundUri,
                backgroundSize = intent.backgroundSize,
                foregroundScale = intent.foregroundScale,
                foregroundOffset = intent.foregroundOffset,
                foregroundRotation = intent.foregroundRotation,
                foregroundSize = intent.foregroundSize,
                borderSize = intent.borderSize,
            ).mapToUIChange { data ->
                SelfieSegmentationEvent(exportUiEvent = ExportUiEvent.Success(data))
            }.defaultFinally()
        },
    )
}