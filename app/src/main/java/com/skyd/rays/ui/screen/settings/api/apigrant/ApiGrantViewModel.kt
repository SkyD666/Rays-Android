package com.skyd.rays.ui.screen.settings.api.apigrant

import com.skyd.rays.base.BaseViewModel
import com.skyd.rays.base.IUIChange
import com.skyd.rays.model.bean.ApiGrantDataBean
import com.skyd.rays.model.bean.EmptyApiGrantDataBean
import com.skyd.rays.model.respository.ApiGrantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.merge
import javax.inject.Inject

@HiltViewModel
class ApiGrantViewModel @Inject constructor(
    private var apiGrantRepo: ApiGrantRepository
) : BaseViewModel<ApiGrantState, ApiGrantEvent, ApiGrantIntent>() {
    override fun initUiState(): ApiGrantState {
        return ApiGrantState(
            apiGrantResultUiState = ApiGrantResultUiState.Init
        )
    }

    override fun IUIChange.checkStateOrEvent() =
        this as? ApiGrantState to this as? ApiGrantEvent

    override fun Flow<ApiGrantIntent>.handleIntent(): Flow<IUIChange> = merge(
        doIsInstance<ApiGrantIntent.GetAllApiGrant> {
            apiGrantRepo.requestAllPackages()
                .mapToUIChange { data ->
                    copy(apiGrantResultUiState = ApiGrantResultUiState.Success(data.reversed()))
                }
                .defaultFinally()
        },

        doIsInstance<ApiGrantIntent.UpdateApiGrant> { intent ->
            apiGrantRepo.requestUpdate(intent.bean)
                .mapToUIChange { data ->
                    if (data is EmptyApiGrantDataBean) {
                        ApiGrantEvent(
                            addPackageNameUiEvent = AddPackageNameUiEvent.Failed(data.msg)
                        )
                    } else if (data is ApiGrantDataBean &&
                        apiGrantResultUiState is ApiGrantResultUiState.Success
                    ) {
                        val dataList = apiGrantResultUiState.data
                        val packageName = intent.bean.packageName
                        val beanIndex = dataList.indexOfFirst {
                            it.apiGrantPackageBean.packageName == packageName
                        }
                        if (beanIndex == -1) {
                            copy(
                                apiGrantResultUiState = ApiGrantResultUiState.Success(
                                    dataList.toMutableList().apply { add(0, data) }
                                )
                            )
                        } else {
                            val newDataBean = dataList[beanIndex]
                                .copy(apiGrantPackageBean = intent.bean)
                            copy(
                                apiGrantResultUiState = ApiGrantResultUiState.Success(
                                    dataList
                                        .toMutableList()
                                        .apply { set(index = beanIndex, element = newDataBean) }
                                )
                            )
                        }
                    } else {
                        this
                    }
                }
                .defaultFinally()
        },

        doIsInstance<ApiGrantIntent.DeleteApiGrant> { intent ->
            apiGrantRepo.requestDelete(intent.packageName)
                .mapToUIChange {
                    if (apiGrantResultUiState is ApiGrantResultUiState.Success) {
                        val dataList = apiGrantResultUiState.data
                        copy(
                            apiGrantResultUiState = ApiGrantResultUiState.Success(
                                dataList.toMutableList()
                                    .apply { removeIf { it.apiGrantPackageBean.packageName == intent.packageName } }
                            )
                        )
                    } else {
                        this
                    }
                }
                .defaultFinally()
        },
    )
}