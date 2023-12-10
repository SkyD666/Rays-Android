package com.skyd.rays.ui.screen.settings.data.importexport.file.exportfiles

import androidx.lifecycle.viewModelScope
import com.skyd.rays.base.mvi.AbstractMviViewModel
import com.skyd.rays.ext.catchMap
import com.skyd.rays.ext.startWith
import com.skyd.rays.model.bean.ImportExportResultInfo
import com.skyd.rays.model.bean.ImportExportWaitingInfo
import com.skyd.rays.model.respository.ImportExportFilesRepository
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
class ExportFilesViewModel @Inject constructor(
    private var importExportFilesRepo: ImportExportFilesRepository
) : AbstractMviViewModel<ExportFilesIntent, ExportFilesState, ExportFilesEvent>() {

    override val viewState: StateFlow<ExportFilesState>

    init {
        val initialVS = ExportFilesState.initial()

        viewState = merge(
            intentSharedFlow.filterIsInstance<ExportFilesIntent.Init>().take(1),
            intentSharedFlow.filterNot { it is ExportFilesIntent.Init }
        )
            .shareWhileSubscribed()
            .toPartialStateChangeFlow()
            .debugLog("ExportFilesPartialStateChange")
            .sendSingleEvent()
            .scan(initialVS) { vs, change -> change.reduce(vs) }
            .debugLog("ViewState")
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                initialVS
            )
    }


    private fun Flow<ExportFilesPartialStateChange>.sendSingleEvent(): Flow<ExportFilesPartialStateChange> {
        return onEach { change ->
            val event = when (change) {
                is ExportFilesPartialStateChange.ExportFilesProgress.Finish ->
                    ExportFilesEvent.ExportResultEvent.Success(change.info)

                is ExportFilesPartialStateChange.ExportFilesProgress.Error ->
                    ExportFilesEvent.ExportResultEvent.Error(change.msg)

                else -> return@onEach
            }
            sendEvent(event)
        }
    }

    private fun SharedFlow<ExportFilesIntent>.toPartialStateChangeFlow(): Flow<ExportFilesPartialStateChange> {
        return merge(
            filterIsInstance<ExportFilesIntent.Init>().map { ExportFilesPartialStateChange.Init },

            filterIsInstance<ExportFilesIntent.Export>().flatMapConcat { intent ->
                importExportFilesRepo.requestExport(intent.dirUri).map {
                    when (it) {
                        is ImportExportResultInfo -> {
                            ExportFilesPartialStateChange.ExportFilesProgress.Finish(it)
                        }

                        is ImportExportWaitingInfo -> {
                            ExportFilesPartialStateChange.ExportFilesProgress.Progressing(it)
                        }
                    }
                }.startWith(ExportFilesPartialStateChange.LoadingDialog)
                    .catchMap { ExportFilesPartialStateChange.ExportFilesProgress.Error(it.message.orEmpty()) }
            },
        )
    }
}