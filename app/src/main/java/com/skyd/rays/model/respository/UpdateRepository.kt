package com.skyd.rays.model.respository

import com.skyd.rays.base.BaseRepository
import com.skyd.rays.model.bean.UpdateBean
import com.skyd.rays.model.service.UpdateService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.Retrofit

class UpdateRepository(private val retrofit: Retrofit) : BaseRepository() {
    fun checkUpdate(): Flow<UpdateBean> = flow {
        emit(retrofit.create(UpdateService::class.java).checkUpdate())
    }.flowOn(Dispatchers.IO)
}