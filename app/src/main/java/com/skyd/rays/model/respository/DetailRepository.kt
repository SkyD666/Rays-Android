package com.skyd.rays.model.respository

import android.net.Uri
import com.skyd.rays.appContext
import com.skyd.rays.base.BaseData
import com.skyd.rays.base.BaseRepository
import com.skyd.rays.ext.dataStore
import com.skyd.rays.ext.getOrDefault
import com.skyd.rays.model.bean.StickerWithTags
import com.skyd.rays.model.db.dao.sticker.StickerDao
import com.skyd.rays.model.preference.ExportStickerDirPreference
import com.skyd.rays.util.exportSticker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class DetailRepository @Inject constructor(private val stickerDao: StickerDao) : BaseRepository() {
    suspend fun requestStickerWithTagsDetail(stickerUuid: String): Flow<BaseData<StickerWithTags>> {
        return flow {
            val stickerWithTags = stickerDao.getStickerWithTags(stickerUuid)
            emitBaseData(BaseData<StickerWithTags>().apply {
                code = if (stickerWithTags == null) 1 else 0
                data = stickerWithTags
            })
        }
    }

    suspend fun requestDeleteStickerWithTagsDetail(stickerUuids: List<String>): Flow<BaseData<Int>> {
        return flow {
            emitBaseData(BaseData<Int>().apply {
                code = 0
                data = stickerDao.deleteStickerWithTags(stickerUuids)
            })
        }
    }

    suspend fun requestAddClickCount(stickerUuid: String, count: Int = 1): Flow<BaseData<Int>> {
        return flow {
            emitBaseData(BaseData<Int>().apply {
                code = 0
                data = stickerDao.addClickCount(uuid = stickerUuid, count = count)
            })
        }
    }

    suspend fun requestExportStickers(stickerUuids: List<String>): Flow<BaseData<Int>> {
        return flow {
            val exportStickerDir = appContext.dataStore.getOrDefault(ExportStickerDirPreference)
            check(exportStickerDir.isNotBlank()) { "exportStickerDir is null" }
            var successCount = 0
            stickerUuids.forEach {
                runCatching {
                    exportSticker(uuid = it, outputDir = Uri.parse(exportStickerDir))
                }.onSuccess {
                    successCount++
                }.onFailure {
                    it.printStackTrace()
                }
            }
            emitBaseData(BaseData<Int>().apply {
                code = 0
                data = successCount
            })
        }
    }
}