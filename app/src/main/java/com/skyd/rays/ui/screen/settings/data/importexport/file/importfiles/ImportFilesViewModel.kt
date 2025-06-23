package com.skyd.rays.ui.screen.settings.data.importexport.file.importfiles

import androidx.lifecycle.viewModelScope
import com.skyd.rays.base.mvi.AbstractMviViewModel
import com.skyd.rays.ext.catchMap
import com.skyd.rays.ext.startWith
import com.skyd.rays.model.bean.ImportExportResultInfo
import com.skyd.rays.model.bean.ImportExportWaitingInfo
import com.skyd.rays.model.respository.ImportExportFilesRepository
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

class ImportFilesViewModel(
    private var importExportFilesRepo: ImportExportFilesRepository
) : AbstractMviViewModel<ImportFilesIntent, ImportFilesState, ImportFilesEvent>() {

    override val viewState: StateFlow<ImportFilesState>

    init {
        val initialVS = ImportFilesState.initial()

        viewState = merge(
            intentSharedFlow.filterIsInstance<ImportFilesIntent.Init>().take(1),
            intentSharedFlow.filterNot { it is ImportFilesIntent.Init }
        )
            .shareWhileSubscribed()
            .toPartialStateChangeFlow()
            .debugLog("ImportFilesPartialStateChange")
            .sendSingleEvent()
            .scan(initialVS) { vs, change -> change.reduce(vs) }
            .debugLog("ViewState")
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                initialVS
            )
    }


    private fun Flow<ImportFilesPartialStateChange>.sendSingleEvent(): Flow<ImportFilesPartialStateChange> {
        return onEach { change ->
            val event = when (change) {
                is ImportFilesPartialStateChange.ImportFilesProgress.Finish ->
                    ImportFilesEvent.ImportResultEvent.Success(change.info)

                is ImportFilesPartialStateChange.ImportFilesProgress.Error ->
                    ImportFilesEvent.ImportResultEvent.Error(change.msg)

                else -> return@onEach
            }
            sendEvent(event)
        }
    }

    private fun SharedFlow<ImportFilesIntent>.toPartialStateChangeFlow(): Flow<ImportFilesPartialStateChange> {
        return merge(
            filterIsInstance<ImportFilesIntent.Init>().map { ImportFilesPartialStateChange.Init },

            filterIsInstance<ImportFilesIntent.Import>().flatMapConcat { intent ->
                importExportFilesRepo.requestImport(intent.backupFileUri, intent.strategy).map {
                    when (it) {
                        is ImportExportResultInfo -> {
                            ImportFilesPartialStateChange.ImportFilesProgress.Finish(it)
                        }

                        is ImportExportWaitingInfo -> {
                            ImportFilesPartialStateChange.ImportFilesProgress.Progressing(it)
                        }
                    }
                }.catchMap {
                    ImportFilesPartialStateChange.ImportFilesProgress.Error(it.message.orEmpty())
                }.startWith(ImportFilesPartialStateChange.LoadingDialog)
            },
        )
    }
}