package com.skyd.rays.ui.screen.settings.api.apigrant

import com.skyd.rays.model.bean.ApiGrantDataBean

internal sealed interface ApiGrantPartialStateChange {
    fun reduce(oldState: ApiGrantState): ApiGrantState

    sealed interface ApiGrantList : ApiGrantPartialStateChange {
        override fun reduce(oldState: ApiGrantState): ApiGrantState {
            return when (this) {
                is Success -> oldState.copy(
                    apiGrantResultState = ApiGrantResultState.Success(data)
                )

                Loading -> oldState.copy(
                    apiGrantResultState = oldState.apiGrantResultState.apply { loading = true }
                )
            }
        }

        data object Loading : ApiGrantList
        data class Success(val data: List<ApiGrantDataBean>) : ApiGrantList
    }

    sealed interface AddPackageName : ApiGrantPartialStateChange {
        data class Success(val data: ApiGrantDataBean) : AddPackageName {
            override fun reduce(oldState: ApiGrantState): ApiGrantState {
                val apiGrantResultState = oldState.apiGrantResultState
                return if (apiGrantResultState is ApiGrantResultState.Success) {
                    val dataList = apiGrantResultState.data
                    val packageName = data.apiGrantPackageBean.packageName
                    val beanIndex = dataList.indexOfFirst {
                        it.apiGrantPackageBean.packageName == packageName
                    }
                    if (beanIndex == -1) {
                        oldState.copy(
                            apiGrantResultState = ApiGrantResultState.Success(
                                dataList.toMutableList().apply { add(0, data) }
                            )
                        )
                    } else {
                        val newDataBean = dataList[beanIndex]
                            .copy(apiGrantPackageBean = data.apiGrantPackageBean)
                        oldState.copy(
                            apiGrantResultState = ApiGrantResultState.Success(
                                dataList.toMutableList()
                                    .apply { set(index = beanIndex, element = newDataBean) }
                            )
                        )
                    }
                } else oldState
            }
        }

        data class Failed(val msg: String) : AddPackageName {
            override fun reduce(oldState: ApiGrantState): ApiGrantState = oldState
        }
    }

    sealed interface Delete : ApiGrantPartialStateChange {
        data class Success(val deletedPackageName: String) : Delete {
            override fun reduce(oldState: ApiGrantState): ApiGrantState {
                val apiGrantResultState = oldState.apiGrantResultState
                return if (apiGrantResultState is ApiGrantResultState.Success) {
                    val dataList = apiGrantResultState.data
                    oldState.copy(
                        apiGrantResultState = ApiGrantResultState.Success(
                            dataList.toMutableList().apply {
                                removeIf {
                                    it.apiGrantPackageBean.packageName == deletedPackageName
                                }
                            }
                        )
                    )
                } else oldState
            }
        }
    }
}
