package com.skyd.rays.model.respository

import android.net.Uri
import com.skyd.rays.base.BaseRepository
import com.skyd.rays.model.bean.StickerWithTags
import com.skyd.rays.model.db.dao.sticker.StickerDao
import com.skyd.rays.util.copyStickerToTempFolder
import com.skyd.rays.util.stickerUuidToFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class MergeStickersRepository(
    private val stickerDao: StickerDao,
    private val addRepo: AddRepository,
    private val searchRepo: SearchRepository,
) : BaseRepository() {
    fun requestStickers(stickerUuids: List<String>): Flow<List<StickerWithTags>> =
        stickerDao.getAllStickerWithTagsList(stickerUuids)

    fun requestMerge(
        oldStickerUuid: String,
        sticker: StickerWithTags,
        deleteUuids: List<String>,
    ): Flow<Any> {
        return flow {
            val stickerFile = stickerUuidToFile(oldStickerUuid)
            check(stickerFile.exists())
            val newStickerFile = stickerFile.copyStickerToTempFolder(fileExtension = false)
            check(newStickerFile.exists())
            emit(newStickerFile)
        }.flatMapConcat { newStickerFile ->
            searchRepo.requestDeleteStickerWithTagsDetail(deleteUuids).flatMapConcat {
                addRepo.requestAddStickerWithTags(sticker, Uri.fromFile(newStickerFile))
            }
        }.flowOn(Dispatchers.IO)
    }
}