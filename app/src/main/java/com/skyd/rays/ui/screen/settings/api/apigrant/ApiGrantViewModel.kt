package com.skyd.rays.ui.screen.settings.api.apigrant

import androidx.lifecycle.viewModelScope
import com.skyd.rays.base.mvi.AbstractMviViewModel
import com.skyd.rays.ext.startWith
import com.skyd.rays.model.bean.ApiGrantDataBean
import com.skyd.rays.model.bean.EmptyApiGrantDataBean
import com.skyd.rays.model.respository.ApiGrantRepository
import com.skyd.rays.ui.screen.search.SearchIntent
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
class ApiGrantViewModel @Inject constructor(
    private var apiGrantRepo: ApiGrantRepository
) : AbstractMviViewModel<ApiGrantIntent, ApiGrantState, ApiGrantEvent>() {

    override val viewState: StateFlow<ApiGrantState>

    init {
        val initialVS = ApiGrantState.initial()

        viewState = merge(
            intentSharedFlow.filterIsInstance<ApiGrantIntent.GetAllApiGrant>().take(1),
            intentSharedFlow.filterNot { it is ApiGrantIntent.GetAllApiGrant }
        )
            .shareWhileSubscribed()
            .toApiGrantPartialStateChangeFlow()
            .debugLog("ApiGrantPartialStateChange")
            .sendSingleEvent()
            .scan(initialVS) { vs, change -> change.reduce(vs) }
            .debugLog("ViewState")
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                initialVS
            )
    }

    private fun Flow<ApiGrantPartialStateChange>.sendSingleEvent(): Flow<ApiGrantPartialStateChange> {
        return onEach { change ->
            val event = when (change) {
                is ApiGrantPartialStateChange.AddPackageName.Success -> {
                    ApiGrantEvent.AddPackageName.Success
                }

                is ApiGrantPartialStateChange.AddPackageName.Failed -> {
                    ApiGrantEvent.AddPackageName.Failed(change.msg)
                }

                else -> return@onEach
            }
            sendEvent(event)
        }
    }

    private fun SharedFlow<ApiGrantIntent>.toApiGrantPartialStateChangeFlow(): Flow<ApiGrantPartialStateChange> {
        return merge(
            filterIsInstance<ApiGrantIntent.GetAllApiGrant>().flatMapConcat {
                apiGrantRepo.requestAllPackages().map {
                    ApiGrantPartialStateChange.ApiGrantList.Success(it.reversed())
                }.startWith(ApiGrantPartialStateChange.ApiGrantList.Loading)
            },

            filterIsInstance<ApiGrantIntent.UpdateApiGrant>().flatMapConcat { intent ->
                apiGrantRepo.requestUpdate(intent.bean).map { data ->
                    when (data) {
                        is ApiGrantDataBean -> {
                            ApiGrantPartialStateChange.AddPackageName.Success(data)
                        }

                        is EmptyApiGrantDataBean -> {
                            ApiGrantPartialStateChange.AddPackageName.Failed(data.msg)
                        }
                    }
                }
            },

            filterIsInstance<ApiGrantIntent.DeleteApiGrant>().flatMapConcat { intent ->
                apiGrantRepo.requestDelete(intent.packageName)
                    .map { ApiGrantPartialStateChange.Delete.Success(it.first) }
            },
        )
    }
}