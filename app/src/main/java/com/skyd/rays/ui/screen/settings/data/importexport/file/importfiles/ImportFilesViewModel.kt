package com.skyd.rays.ui.screen.settings.data.importexport.file.importfiles

import com.skyd.rays.base.BaseViewModel
import com.skyd.rays.base.IUIChange
import com.skyd.rays.base.IUiState
import com.skyd.rays.model.respository.ImportExportFilesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.merge
import javax.inject.Inject

@HiltViewModel
class ImportFilesViewModel @Inject constructor(
    private var importExportFilesRepo: ImportExportFilesRepository
) : BaseViewModel<IUiState, ImportFilesEvent, ImportFilesIntent>() {
    override fun initUiState(): IUiState {
        return object : IUiState {}
    }

    override fun IUIChange.checkStateOrEvent() = this as? IUiState to this as? ImportFilesEvent

    override fun Flow<ImportFilesIntent>.handleIntent(): Flow<IUIChange> = merge(
        doIsInstance<ImportFilesIntent.Import> { intent ->
            importExportFilesRepo.requestImport(
                intent.backupFileUri,
                intent.proxy
            )
                .mapToUIChange { data ->
                    ImportFilesEvent(importResultUiEvent = ImportResultUiEvent.Success(data))
                }
                .defaultFinally()
        },
    )
}