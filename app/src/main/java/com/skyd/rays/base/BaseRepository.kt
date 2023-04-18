package com.skyd.rays.base

import kotlinx.coroutines.flow.FlowCollector

open class BaseRepository {
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