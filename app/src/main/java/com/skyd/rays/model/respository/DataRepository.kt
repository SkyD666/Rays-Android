package com.skyd.rays.model.respository

import com.skyd.rays.base.BaseData
import com.skyd.rays.base.BaseRepository
import com.skyd.rays.model.db.dao.sticker.StickerDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class DataRepository @Inject constructor(private val stickerDao: StickerDao) : BaseRepository() {
    suspend fun requestDeleteAllData(): Flow<BaseData<Long>> {
        return flow {
            val startTime = System.currentTimeMillis()
            stickerDao.deleteAllStickerWithTags()
            emitBaseData(BaseData<Long>().apply {
                code = 0
                data = System.currentTimeMillis() - startTime
            })
        }
    }
}