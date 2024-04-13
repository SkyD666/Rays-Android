package com.skyd.rays.model.respository

import com.skyd.rays.appContext
import com.skyd.rays.base.BaseRepository
import com.skyd.rays.config.PROVIDER_THUMBNAIL_DIR
import com.skyd.rays.model.db.dao.cache.StickerShareTimeDao
import com.skyd.rays.model.db.dao.sticker.StickerDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import kotlin.system.measureTimeMillis

class DataRepository @Inject constructor(
    private val stickerDao: StickerDao,
    private val stickerShareTimeDao: StickerShareTimeDao,
) : BaseRepository() {
    suspend fun requestDeleteAllData(): Flow<Long> {
        return flowOnIo {
            emit(measureTimeMillis { stickerDao.deleteAllStickerWithTags() })
        }
    }

    suspend fun requestDeleteStickerShareTime(): Flow<Long> {
        return flowOnIo {
            emit(measureTimeMillis { stickerShareTimeDao.deleteAll() })
        }
    }

    suspend fun requestDeleteDocumentsProviderThumbnails(): Flow<Long> {
        return flowOnIo {
            emit(measureTimeMillis { appContext.PROVIDER_THUMBNAIL_DIR.deleteRecursively() })
        }
    }
}