package com.skyd.rays.model.respository

import com.skyd.rays.base.BaseRepository
import com.skyd.rays.model.bean.UpdateBean
import com.skyd.rays.model.service.UpdateService
import kotlinx.coroutines.flow.Flow
import retrofit2.Retrofit
import javax.inject.Inject

class UpdateRepository @Inject constructor(private val retrofit: Retrofit) : BaseRepository() {
    fun checkUpdate(): Flow<UpdateBean> {
        return flowOnIo {
            emit(retrofit.create(UpdateService::class.java).checkUpdate())
        }
    }
}