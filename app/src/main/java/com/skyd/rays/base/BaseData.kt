package com.skyd.rays.base

class BaseData<T> {
    var code = -1
    var msg: String? = null
    var data: T? = null
    var state: ReqState = ReqState.Error
}

enum class ReqState {
    Success, Error
}