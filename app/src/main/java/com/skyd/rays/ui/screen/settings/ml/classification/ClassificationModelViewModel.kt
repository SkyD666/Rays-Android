package com.skyd.rays.ui.screen.settings.ml.classification

import androidx.lifecycle.viewModelScope
import com.skyd.rays.appContext
import com.skyd.rays.base.BaseViewModel
import com.skyd.rays.base.IUIChange
import com.skyd.rays.base.IUiEvent
import com.skyd.rays.model.preference.StickerClassificationModelPreference
import com.skyd.rays.model.respository.ClassificationModelRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flattenConcat
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import javax.inject.Inject

@HiltViewModel
class ClassificationModelViewModel @Inject constructor(
    private var classificationModelRepo: ClassificationModelRepository
) : BaseViewModel<ClassificationModelState, IUiEvent, ClassificationModelIntent>() {
    override fun initUiState(): ClassificationModelState {
        return ClassificationModelState(
            GetModelsUiState.Init,
        )
    }

    override fun IUIChange.checkStateOrEvent() =
        this as? ClassificationModelState? to this as? IUiEvent

    override fun Flow<ClassificationModelIntent>.handleIntent(): Flow<IUIChange> = merge(
        doIsInstance<ClassificationModelIntent.GetModels> {
            classificationModelRepo.requestGetModels()
                .mapToUIChange { data ->
                    copy(getModelsUiState = GetModelsUiState.Success(data))
                }
                .defaultFinally()
        },

        doIsInstance<ClassificationModelIntent.SetModel> { intent ->
            classificationModelRepo.requestSetModel(intent.modelUri)
                .mapToUIChange { data ->
                    StickerClassificationModelPreference.put(
                        context = appContext,
                        scope = viewModelScope,
                        value = data
                    )
                    object : IUiEvent {}
                }
                .defaultFinally()
        },

        doIsInstance<ClassificationModelIntent.ImportModel> { intent ->
            classificationModelRepo.requestImportModel(intent.modelUri)
                .map {
                    classificationModelRepo.requestGetModels()
                }.flattenConcat().mapToUIChange { data ->
                    copy(getModelsUiState = GetModelsUiState.Success(data))
                }
                .defaultFinally()
        },

        doIsInstance<ClassificationModelIntent.DeleteModel> { intent ->
            classificationModelRepo.requestDeleteModel(intent.modelUri)
                .map {
                    if (it.data == true) {
                        StickerClassificationModelPreference.put(
                            context = appContext,
                            scope = viewModelScope,
                            value = StickerClassificationModelPreference.default
                        )
                    }
                    classificationModelRepo.requestGetModels()
                }.flattenConcat().mapToUIChange { data ->
                    copy(getModelsUiState = GetModelsUiState.Success(data))
                }
                .defaultFinally()
        },
    )
}