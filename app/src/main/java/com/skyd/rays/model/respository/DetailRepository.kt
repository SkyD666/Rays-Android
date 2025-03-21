package com.skyd.rays.model.respository

import androidx.core.net.toUri
import com.skyd.rays.appContext
import com.skyd.rays.base.BaseRepository
import com.skyd.rays.ext.dataStore
import com.skyd.rays.ext.getOrDefault
import com.skyd.rays.model.bean.StickerWithTags
import com.skyd.rays.model.db.dao.sticker.StickerDao
import com.skyd.rays.model.preference.ExportStickerDirPreference
import com.skyd.rays.util.exportSticker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class DetailRepository @Inject constructor(private val stickerDao: StickerDao) : BaseRepository() {
    fun requestStickerWithTagsDetail(
        stickerUuid: String,
        addClickCount: Int = 1,
    ): Flow<StickerWithTags?> = flowOf(stickerUuid).filter {
        it.isNotBlank()
    }.flatMapConcat { uuid ->
        stickerDao.addClickCount(uuid = uuid, count = addClickCount)
        stickerDao.getStickerWithTagsFlow(uuid)
    }.flowOn(Dispatchers.IO)

    fun requestDeleteStickerWithTagsDetail(stickerUuid: String): Flow<Int> = flow {
        emit(stickerDao.deleteStickerWithTags(listOf(stickerUuid)))
    }.flowOn(Dispatchers.IO)

    fun requestExportStickers(stickerUuid: String): Flow<Int> = flow {
        val exportStickerDir = appContext.dataStore.getOrDefault(ExportStickerDirPreference)
        check(exportStickerDir.isNotBlank()) { "exportStickerDir is null" }
        var successCount = 0
        runCatching {
            exportSticker(uuid = stickerUuid, outputDir = exportStickerDir.toUri())
        }.onSuccess {
            successCount++
        }.onFailure {
            it.printStackTrace()
        }
        emit(successCount)
    }.flowOn(Dispatchers.IO)
}