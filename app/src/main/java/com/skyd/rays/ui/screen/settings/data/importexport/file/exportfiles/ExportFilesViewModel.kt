package com.skyd.rays.ui.screen.settings.data.importexport.file.exportfiles

import com.skyd.rays.base.BaseViewModel
import com.skyd.rays.base.IUIChange
import com.skyd.rays.base.IUiState
import com.skyd.rays.model.respository.ImportExportFilesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.merge
import javax.inject.Inject

@HiltViewModel
class ExportFilesViewModel @Inject constructor(
    private var importExportFilesRepo: ImportExportFilesRepository
) : BaseViewModel<IUiState, ExportFilesEvent, ExportFilesIntent>() {
    override fun initUiState(): IUiState {
        return object : IUiState {}
    }

    override fun IUIChange.checkStateOrEvent() = this as? IUiState to this as? ExportFilesEvent

    override fun Flow<ExportFilesIntent>.handleIntent(): Flow<IUIChange> = merge(
        doIsInstance<ExportFilesIntent.Export> { intent ->
            importExportFilesRepo.requestExport(intent.dirUri)
                .mapToUIChange { data ->
                    ExportFilesEvent(exportResultUiEvent = ExportResultUiEvent.Success(data))
                }
                .defaultFinally()
        },
    )
}