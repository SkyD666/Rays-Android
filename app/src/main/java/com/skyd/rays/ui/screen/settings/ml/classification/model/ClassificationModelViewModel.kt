package com.skyd.rays.ui.screen.settings.ml.classification.model

import androidx.lifecycle.viewModelScope
import com.skyd.rays.base.mvi.AbstractMviViewModel
import com.skyd.rays.ext.startWith
import com.skyd.rays.model.respository.ClassificationModelRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ClassificationModelViewModel @Inject constructor(
    private var classificationModelRepo: ClassificationModelRepository
) : AbstractMviViewModel<ClassificationModelIntent, ClassificationModelState, ClassificationModelEvent>() {

    override val viewState: StateFlow<ClassificationModelState>

    init {
        val initialVS = ClassificationModelState.initial()

        viewState = intentSharedFlow
            .toClassificationModelStateChangeFlow()
            .debugLog("ClassificationModelPartialStateChange")
            .sendSingleEvent()
            .scan(initialVS) { vs, change -> change.reduce(vs) }
            .debugLog("ViewState")
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                initialVS
            )
    }

    private fun Flow<ClassificationModelPartialStateChange>.sendSingleEvent(): Flow<ClassificationModelPartialStateChange> {
        return onEach { change ->
            val event = when (change) {
                is ClassificationModelPartialStateChange.Import.Success -> {
                    ClassificationModelEvent.ImportEvent.Success(change.newModel)
                }

                is ClassificationModelPartialStateChange.Import.Failed -> {
                    ClassificationModelEvent.ImportEvent.Failed(change.msg)
                }

                is ClassificationModelPartialStateChange.Delete.Success -> {
                    ClassificationModelEvent.DeleteEvent.Success(change.deletedUri)
                }

                is ClassificationModelPartialStateChange.Delete.Failed -> {
                    ClassificationModelEvent.DeleteEvent.Failed(change.msg)
                }

                else -> return@onEach
            }
            sendEvent(event)
        }
    }

    private fun SharedFlow<ClassificationModelIntent>.toClassificationModelStateChangeFlow()
            : Flow<ClassificationModelPartialStateChange> {
        return merge(
            filterIsInstance<ClassificationModelIntent.GetModels>().flatMapConcat {
                classificationModelRepo.requestGetModels()
                    .map { ClassificationModelPartialStateChange.GetModels.Success(it) }
                    .startWith(ClassificationModelPartialStateChange.LoadingDialog)
            },

            filterIsInstance<ClassificationModelIntent.ImportModel>().flatMapConcat { intent ->
                classificationModelRepo.requestImportModel(intent.uri)
                    .map { ClassificationModelPartialStateChange.Import.Success(it) }
                    .catch { ClassificationModelPartialStateChange.Import.Failed(it.message.toString()) }
                    .startWith(ClassificationModelPartialStateChange.LoadingDialog)
            },

            filterIsInstance<ClassificationModelIntent.SetModel>().flatMapConcat { intent ->
                classificationModelRepo.requestSetModel(intent.modelBean.uri)
                    .map { ClassificationModelPartialStateChange.SetModel.Success }
                    .startWith(ClassificationModelPartialStateChange.LoadingDialog)
            },

            filterIsInstance<ClassificationModelIntent.DeleteModel>().flatMapConcat { intent ->
                classificationModelRepo.requestDeleteModel(intent.modelBean.uri)
                    .map { ClassificationModelPartialStateChange.Delete.Success(it) }
                    .catch { ClassificationModelPartialStateChange.Delete.Failed(it.message.toString()) }
                    .startWith(ClassificationModelPartialStateChange.LoadingDialog)
            },
        )
    }
}