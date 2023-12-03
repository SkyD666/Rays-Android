package com.skyd.rays.ui.screen.settings.shareconfig.uristringshare

import com.skyd.rays.model.bean.UriStringShareDataBean

internal sealed interface UriStringSharePartialStateChange {
    fun reduce(oldState: UriStringShareState): UriStringShareState

    sealed interface GetAllUriStringShare : UriStringSharePartialStateChange {
        override fun reduce(oldState: UriStringShareState): UriStringShareState {
            return when (this) {
                is Success -> oldState.copy(
                    uriStringShareResultState = UriStringShareResultState.Success(data),
                    loadingDialog = false,
                )

                Loading -> oldState.copy(
                    loadingDialog = true,
                )
            }
        }

        data object Loading : GetAllUriStringShare
        data class Success(val data: List<UriStringShareDataBean>) : GetAllUriStringShare
    }

    sealed interface UpdateUriStringShare : UriStringSharePartialStateChange {
        override fun reduce(oldState: UriStringShareState): UriStringShareState {
            return when (this) {
                is Success -> {
                    if (oldState.uriStringShareResultState is UriStringShareResultState.Success) {
                        val dataList = oldState.uriStringShareResultState.data
                        val packageName = data.uriStringSharePackageBean.packageName
                        val beanIndex = dataList.indexOfFirst {
                            it.uriStringSharePackageBean.packageName == packageName
                        }
                        if (beanIndex == -1) {
                            oldState.copy(
                                uriStringShareResultState = UriStringShareResultState.Success(
                                    dataList.toMutableList().apply { add(0, data) }
                                ),
                                loadingDialog = false,
                            )
                        } else {
                            val newDataBean = dataList[beanIndex]
                                .copy(uriStringSharePackageBean = data.uriStringSharePackageBean)
                            oldState.copy(
                                uriStringShareResultState = UriStringShareResultState.Success(
                                    dataList
                                        .toMutableList()
                                        .apply { set(index = beanIndex, element = newDataBean) }
                                ),
                                loadingDialog = false,
                            )
                        }
                    } else oldState
                }

                is Failed -> oldState.copy(loadingDialog = false)
            }
        }

        data class Failed(val msg: String) : UpdateUriStringShare
        data class Success(val data: UriStringShareDataBean) : UpdateUriStringShare
    }

    sealed interface Delete : UriStringSharePartialStateChange {
        override fun reduce(oldState: UriStringShareState): UriStringShareState {
            return when (this) {
                is Success -> {
                    if (oldState.uriStringShareResultState is UriStringShareResultState.Success) {
                        val dataList = oldState.uriStringShareResultState.data
                        oldState.copy(
                            uriStringShareResultState = UriStringShareResultState.Success(
                                dataList.toMutableList().apply {
                                    removeIf {
                                        it.uriStringSharePackageBean.packageName == packageName
                                    }
                                }
                            ),
                            loadingDialog = false,
                        )
                    } else oldState.copy(
                        loadingDialog = false,
                    )
                }

                Loading -> oldState.copy(
                    loadingDialog = true,
                )
            }
        }

        data object Loading : Delete
        data class Success(val packageName: String, val data: Int) : Delete
    }
}
