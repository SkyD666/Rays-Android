package com.skyd.rays.base

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

open class BaseRepository {
    protected fun <T> flowOnIo(block: suspend FlowCollector<T>.() -> Unit) =
        flow(block).flowOn(Dispatchers.IO)

    protected suspend fun <T : Any> FlowCollector<BaseData<T>>.emitBaseData(baseData: BaseData<T>) {
        return emit(checkBaseData(baseData))
    }

    protected fun <T : Any> checkBaseData(baseData: BaseData<T>): BaseData<T> {
        if (baseData.code == 0) {
            baseData.state = ReqState.Success
        } else {
            baseData.state = ReqState.Error
        }
        return baseData
    }
}