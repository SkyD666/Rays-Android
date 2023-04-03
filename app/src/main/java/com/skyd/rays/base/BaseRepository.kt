package com.skyd.rays.base

import kotlinx.coroutines.flow.FlowCollector

open class BaseRepository {
    protected suspend fun <T : Any> FlowCollector<BaseData<T>>.emitBaseData(baseData: BaseData<T>) {
        if (baseData.code == 0) {
            baseData.state = ReqState.Success
        } else {
            baseData.state = ReqState.Error
        }
        return emit(baseData)
    }
}