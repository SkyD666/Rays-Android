package com.skyd.rays.model.respository

import com.skyd.rays.base.BaseData
import com.skyd.rays.base.BaseRepository
import com.skyd.rays.model.bean.UpdateBean
import com.skyd.rays.model.service.UpdateService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Retrofit
import javax.inject.Inject

class UpdateRepository @Inject constructor(private val retrofit: Retrofit) : BaseRepository() {
    suspend fun checkUpdate(): Flow<BaseData<UpdateBean>> {
        return flow {
            emitBaseData(BaseData<UpdateBean>().apply {
                code = 0
                data = retrofit.create(UpdateService::class.java).checkUpdate()
            })
        }
    }
}