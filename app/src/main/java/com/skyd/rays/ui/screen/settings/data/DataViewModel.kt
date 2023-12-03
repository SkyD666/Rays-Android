package com.skyd.rays.ui.screen.settings.data

import androidx.lifecycle.viewModelScope
import com.skyd.rays.base.mvi.AbstractMviViewModel
import com.skyd.rays.ext.startWith
import com.skyd.rays.model.respository.DataRepository
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
class DataViewModel @Inject constructor(private var dataRepo: DataRepository) :
    AbstractMviViewModel<DataIntent, DataState, DataEvent>() {

    override val viewState: StateFlow<DataState>

    init {
        val initialVS = DataState.initial()

        viewState = merge(
            intentSharedFlow.filterIsInstance<DataIntent.Init>().take(1),
            intentSharedFlow.filterNot { it is DataIntent.Init }
        )
            .shareWhileSubscribed()
            .toPartialStateChangeFlow()
            .debugLog("DataPartialStateChange")
            .sendSingleEvent()
            .scan(initialVS) { vs, change -> change.reduce(vs) }
            .debugLog("ViewState")
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                initialVS
            )
    }


    private fun Flow<DataPartialStateChange>.sendSingleEvent(): Flow<DataPartialStateChange> {
        return onEach { change ->
            val event = when (change) {
                is DataPartialStateChange.DeleteAllData.Success ->
                    DataEvent.DeleteAllResultEvent.Success(change.time)

                else -> return@onEach
            }
            sendEvent(event)
        }
    }

    private fun SharedFlow<DataIntent>.toPartialStateChangeFlow(): Flow<DataPartialStateChange> {
        return merge(
            filterIsInstance<DataIntent.Init>().map { DataPartialStateChange.Init },

            filterIsInstance<DataIntent.DeleteAllData>().flatMapConcat {
                dataRepo.requestDeleteAllData()
                    .map { DataPartialStateChange.DeleteAllData.Success(it) }
                    .startWith(DataPartialStateChange.LoadingDialog)
            },
        )
    }
}