package com.skyd.rays.model.respository

import com.skyd.rays.base.BaseRepository
import com.skyd.rays.config.PROVIDER_THUMBNAIL_DIR
import com.skyd.rays.model.db.dao.sticker.StickerDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import kotlin.system.measureTimeMillis

class DataRepository @Inject constructor(private val stickerDao: StickerDao) : BaseRepository() {
    suspend fun requestDeleteAllData(): Flow<Long> {
        return flowOnIo {
            emit(measureTimeMillis { stickerDao.deleteAllStickerWithTags() })
        }
    }

    suspend fun requestDeleteDocumentsProviderThumbnails(): Flow<Long> {
        return flowOnIo {
            emit(measureTimeMillis {
                PROVIDER_THUMBNAIL_DIR.deleteRecursively()
            })
        }
    }
}