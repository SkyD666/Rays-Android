package com.skyd.rays.model.respository

import android.net.Uri
import com.skyd.rays.appContext
import com.skyd.rays.base.BaseRepository
import com.skyd.rays.ext.dataStore
import com.skyd.rays.ext.getOrDefault
import com.skyd.rays.model.bean.StickerWithTags
import com.skyd.rays.model.db.dao.sticker.StickerDao
import com.skyd.rays.model.preference.ExportStickerDirPreference
import com.skyd.rays.util.exportSticker
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DetailRepository @Inject constructor(private val stickerDao: StickerDao) : BaseRepository() {
    suspend fun requestStickerWithTagsDetail(stickerUuid: String): Flow<StickerWithTags?> {
        return flowOnIo {
            val stickerWithTags = if (stickerUuid.isBlank()) null
            else stickerDao.getStickerWithTags(stickerUuid)
            if (stickerWithTags != null) stickerDao.addClickCount(uuid = stickerUuid, count = 1)
            emit(stickerWithTags)
        }
    }

    suspend fun requestDeleteStickerWithTagsDetail(stickerUuid: String): Flow<Int> {
        return flowOnIo {
            emit(stickerDao.deleteStickerWithTags(listOf(stickerUuid)))
        }
    }

    suspend fun requestExportStickers(stickerUuid: String): Flow<Int> {
        return flowOnIo {
            val exportStickerDir = appContext.dataStore.getOrDefault(ExportStickerDirPreference)
            check(exportStickerDir.isNotBlank()) { "exportStickerDir is null" }
            var successCount = 0
            runCatching {
                exportSticker(uuid = stickerUuid, outputDir = Uri.parse(exportStickerDir))
            }.onSuccess {
                successCount++
            }.onFailure {
                it.printStackTrace()
            }
            emit(successCount)
        }
    }
}