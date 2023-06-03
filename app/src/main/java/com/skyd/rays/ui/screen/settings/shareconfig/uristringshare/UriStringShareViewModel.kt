package com.skyd.rays.ui.screen.settings.shareconfig.uristringshare

import com.skyd.rays.base.BaseViewModel
import com.skyd.rays.base.IUIChange
import com.skyd.rays.model.bean.EmptyUriStringShareDataBean
import com.skyd.rays.model.bean.UriStringShareDataBean
import com.skyd.rays.model.respository.UriStringShareRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.merge
import javax.inject.Inject

@HiltViewModel
class UriStringShareViewModel @Inject constructor(
    private var uriStringShareRepo: UriStringShareRepository
) : BaseViewModel<UriStringShareState, UriStringShareEvent, UriStringShareIntent>() {
    override fun initUiState(): UriStringShareState {
        return UriStringShareState(
            uriStringShareResultUiState = UriStringShareResultUiState.Init
        )
    }

    override fun IUIChange.checkStateOrEvent() =
        this as? UriStringShareState to this as? UriStringShareEvent

    override fun Flow<UriStringShareIntent>.handleIntent(): Flow<IUIChange> = merge(
        doIsInstance<UriStringShareIntent.GetAllUriStringShare> {
            uriStringShareRepo.requestAllPackages()
                .mapToUIChange { data ->
                    copy(uriStringShareResultUiState = UriStringShareResultUiState.Success(data.reversed()))
                }
                .defaultFinally()
        },

        doIsInstance<UriStringShareIntent.UpdateUriStringShare> { intent ->
            uriStringShareRepo.requestUpdate(intent.bean)
                .mapToUIChange { data ->
                    if (data is EmptyUriStringShareDataBean) {
                        UriStringShareEvent(
                            addPackageNameUiEvent = AddPackageNameUiEvent.Failed(data.msg)
                        )
                    } else if (data is UriStringShareDataBean &&
                        uriStringShareResultUiState is UriStringShareResultUiState.Success
                    ) {
                        val dataList = uriStringShareResultUiState.data
                        val packageName = intent.bean.packageName
                        val beanIndex = dataList.indexOfFirst {
                            it.uriStringSharePackageBean.packageName == packageName
                        }
                        if (beanIndex == -1) {
                            copy(
                                uriStringShareResultUiState = UriStringShareResultUiState.Success(
                                    dataList.toMutableList().apply { add(0, data) }
                                )
                            )
                        } else {
                            val newDataBean = dataList[beanIndex]
                                .copy(uriStringSharePackageBean = intent.bean)
                            copy(
                                uriStringShareResultUiState = UriStringShareResultUiState.Success(
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

        doIsInstance<UriStringShareIntent.DeleteUriStringShare> { intent ->
            uriStringShareRepo.requestDelete(intent.packageName)
                .mapToUIChange {
                    if (uriStringShareResultUiState is UriStringShareResultUiState.Success) {
                        val dataList = uriStringShareResultUiState.data
                        copy(
                            uriStringShareResultUiState = UriStringShareResultUiState.Success(
                                dataList.toMutableList()
                                    .apply { removeIf { it.uriStringSharePackageBean.packageName == intent.packageName } }
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