package com.skyd.rays.model.respository

import android.net.Uri
import com.skyd.rays.base.BaseData
import com.skyd.rays.base.BaseRepository
import com.skyd.rays.config.STICKER_DIR
import com.skyd.rays.db.dao.StickerDao
import com.skyd.rays.ext.copyTo
import com.skyd.rays.ext.md5
import com.skyd.rays.model.bean.StickerWithTags
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File
import javax.inject.Inject
import kotlin.random.Random

class AddRepository @Inject constructor(private val stickerDao: StickerDao) : BaseRepository() {
    suspend fun requestAddStickerWithTags(
        stickerWithTags: StickerWithTags,
        uri: Uri
    ): Flow<BaseData<String>> {
        return flow {
            val tempFile = File(STICKER_DIR, "${Random.nextLong()}")
            uri.copyTo(tempFile)
            stickerWithTags.sticker.stickerMd5 =
                tempFile.md5() ?: error("can not calc sticker's md5!")
            val uuid = stickerDao.addStickerWithTags(stickerWithTags)
            tempFile.renameTo(File(STICKER_DIR, uuid))
            tempFile.deleteRecursively()
            emitBaseData(BaseData<String>().apply {
                code = 0
                data = uuid
            })
        }
    }

    suspend fun requestGetStickerWithTags(stickerUuid: String): Flow<BaseData<StickerWithTags>> {
        return flow {
            emitBaseData(BaseData<StickerWithTags>().apply {
                code = 0
                data = stickerDao.getStickerWithTags(stickerUuid)
            })
        }
    }
}