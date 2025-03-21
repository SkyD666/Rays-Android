package com.skyd.rays.model.respository

import com.skyd.rays.appContext
import com.skyd.rays.base.BaseRepository
import com.skyd.rays.config.PROVIDER_THUMBNAIL_DIR
import com.skyd.rays.model.db.dao.cache.StickerShareTimeDao
import com.skyd.rays.model.db.dao.sticker.MimeTypeDao
import com.skyd.rays.model.db.dao.sticker.StickerDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File
import javax.inject.Inject
import kotlin.system.measureTimeMillis

class DataRepository @Inject constructor(
    private val stickerDao: StickerDao,
    private val stickerShareTimeDao: StickerShareTimeDao,
    private val mimeTypeDao: MimeTypeDao,
) : BaseRepository() {
    fun requestDeleteAllData(): Flow<Long> = flow {
        emit(measureTimeMillis { stickerDao.deleteAllStickerWithTags() })
    }.flowOn(Dispatchers.IO)

    fun requestDeleteStickerShareTime(): Flow<Long> = flow {
        emit(measureTimeMillis { stickerShareTimeDao.deleteAll() })
    }.flowOn(Dispatchers.IO)

    fun requestDeleteDocumentsProviderThumbnails(): Flow<Long> = flow {
        emit(measureTimeMillis { appContext.PROVIDER_THUMBNAIL_DIR.deleteRecursively() })
    }.flowOn(Dispatchers.IO)

    fun requestDeleteAllMimetypes(): Flow<Long> = flow {
        emit(measureTimeMillis { mimeTypeDao.deleteAll() })
    }.flowOn(Dispatchers.IO)

    fun requestDeleteVectorDbFiles(): Flow<Long> = flow {
        emit(measureTimeMillis {
            check(File(appContext.filesDir, "objectbox").deleteRecursively())
        })
    }.flowOn(Dispatchers.IO)
}