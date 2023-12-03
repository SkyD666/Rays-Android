package com.skyd.rays.model.respository

import com.skyd.rays.base.BaseRepository
import com.skyd.rays.model.db.dao.sticker.StickerDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DataRepository @Inject constructor(private val stickerDao: StickerDao) : BaseRepository() {
    suspend fun requestDeleteAllData(): Flow<Long> {
        return flowOnIo {
            val startTime = System.currentTimeMillis()
            stickerDao.deleteAllStickerWithTags()
            emit(System.currentTimeMillis() - startTime)
        }
    }
}