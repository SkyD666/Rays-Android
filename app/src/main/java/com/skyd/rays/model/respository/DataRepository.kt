package com.skyd.rays.model.respository

import com.skyd.rays.appContext
import com.skyd.rays.base.BaseRepository
import com.skyd.rays.config.PROVIDER_THUMBNAIL_DIR
import com.skyd.rays.model.db.dao.cache.StickerShareTimeDao
import com.skyd.rays.model.db.dao.sticker.MimeTypeDao
import com.skyd.rays.model.db.dao.sticker.StickerDao
import kotlinx.coroutines.flow.Flow
import java.io.File
import javax.inject.Inject
import kotlin.system.measureTimeMillis

class DataRepository @Inject constructor(
    private val stickerDao: StickerDao,
    private val stickerShareTimeDao: StickerShareTimeDao,
    private val mimeTypeDao: MimeTypeDao,
) : BaseRepository() {
    fun requestDeleteAllData(): Flow<Long> = flowOnIo {
        emit(measureTimeMillis { stickerDao.deleteAllStickerWithTags() })
    }

    fun requestDeleteStickerShareTime(): Flow<Long> = flowOnIo {
        emit(measureTimeMillis { stickerShareTimeDao.deleteAll() })
    }

    fun requestDeleteDocumentsProviderThumbnails(): Flow<Long> = flowOnIo {
        emit(measureTimeMillis { appContext.PROVIDER_THUMBNAIL_DIR.deleteRecursively() })
    }

    fun requestDeleteAllMimetypes(): Flow<Long> = flowOnIo {
        emit(measureTimeMillis { mimeTypeDao.deleteAll() })
    }

    fun requestDeleteVectorDbFiles(): Flow<Long> = flowOnIo {
        emit(measureTimeMillis {
            check(File(appContext.filesDir, "objectbox").deleteRecursively())
        })
    }
}