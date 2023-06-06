package com.skyd.rays.ui.screen.settings.ml.classification

import com.skyd.rays.base.BaseViewModel
import com.skyd.rays.base.IUIChange
import com.skyd.rays.base.IUiEvent
import com.skyd.rays.base.LoadUiIntent
import com.skyd.rays.model.respository.ClassificationModelRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.merge
import javax.inject.Inject

@HiltViewModel
class ClassificationModelViewModel @Inject constructor(
    private var classificationModelRepo: ClassificationModelRepository
) : BaseViewModel<ClassificationModelState, ClassificationModelEvent, ClassificationModelIntent>() {
    override fun initUiState(): ClassificationModelState {
        return ClassificationModelState(
            GetModelsUiState.Init,
        )
    }

    override fun IUIChange.checkStateOrEvent() =
        this as? ClassificationModelState? to this as? ClassificationModelEvent

    override fun Flow<ClassificationModelIntent>.handleIntent(): Flow<IUIChange> = merge(
        doIsInstance<ClassificationModelIntent.GetModels> {
            classificationModelRepo.requestGetModels()
                .mapToUIChange { data ->
                    copy(getModelsUiState = GetModelsUiState.Success(data))
                }
                .defaultFinally()
        },

        doIsInstance<ClassificationModelIntent.SetModel> { intent ->
            classificationModelRepo.requestSetModel(intent.modelBean.uri)
                .mapToUIChange {
                    object : IUiEvent {}
                }
                .defaultFinally()
        },

        doIsInstance<ClassificationModelIntent.ImportModel> { intent ->
            classificationModelRepo.requestImportModel(intent.uri)
                .mapToUIChange {
                    ClassificationModelEvent(importUiEvent = ImportUiEvent.Success(intent.uri))
                }
                .catch {
                    it.printStackTrace()
                    sendLoadUiIntent(LoadUiIntent.Error(it.message.toString()))
                }
        },

        doIsInstance<ClassificationModelIntent.DeleteModel> { intent ->
            classificationModelRepo.requestDeleteModel(intent.modelBean.uri)
                .mapToUIChange {
                    ClassificationModelEvent(deleteUiEvent = DeleteUiEvent.Success(intent.modelBean.path))
                }
                .catch {
                    it.printStackTrace()
                    sendLoadUiIntent(LoadUiIntent.Error(it.message.toString()))
                }
        },
    )
}