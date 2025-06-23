package com.skyd.rays.ui.screen.settings.data

import androidx.lifecycle.viewModelScope
import com.skyd.rays.base.mvi.AbstractMviViewModel
import com.skyd.rays.ext.catchMap
import com.skyd.rays.ext.startWith
import com.skyd.rays.model.respository.DataRepository
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

class DataViewModel(private var dataRepo: DataRepository) :
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

                is DataPartialStateChange.DeleteStickerShareTime.Success ->
                    DataEvent.DeleteStickerShareTimeResultEvent.Success(change.time)

                is DataPartialStateChange.DeleteVectorDbFiles.Success ->
                    DataEvent.DeleteVectorDbFilesResultEvent.Success(change.time)

                is DataPartialStateChange.DeleteVectorDbFiles.Failed ->
                    DataEvent.DeleteVectorDbFilesResultEvent.Failed(change.msg)

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
            filterIsInstance<DataIntent.DeleteStickerShareTime>().flatMapConcat {
                dataRepo.requestDeleteStickerShareTime()
                    .map { DataPartialStateChange.DeleteStickerShareTime.Success(it) }
                    .startWith(DataPartialStateChange.LoadingDialog)
            },
            filterIsInstance<DataIntent.DeleteVectorDbFiles>().flatMapConcat {
                dataRepo.requestDeleteVectorDbFiles()
                    .map { DataPartialStateChange.DeleteVectorDbFiles.Success(it) }
                    .startWith(DataPartialStateChange.LoadingDialog)
                    .catchMap { DataPartialStateChange.DeleteVectorDbFiles.Failed(it.message.orEmpty()) }
            },
        )
    }
}