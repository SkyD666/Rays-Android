package com.skyd.rays.model.respository

import com.skyd.rays.base.BaseRepository
import com.skyd.rays.model.bean.StickerWithTags
import com.skyd.rays.model.db.dao.sticker.StickerDao
import com.skyd.rays.util.stickerUuidToFile
import com.skyd.rays.util.stickerUuidToUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.util.UUID
import javax.inject.Inject

class MergeStickersRepository @Inject constructor(
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
    ): Flow<Unit> {
        return flow {
            val stickerFile = stickerUuidToFile(oldStickerUuid)
            check(stickerFile.exists())
            if (sticker.sticker.uuid == oldStickerUuid) {
                sticker.sticker.uuid = UUID.randomUUID().toString()
            }
            val newStickerFile = stickerUuidToFile(sticker.sticker.uuid)
            stickerFile.copyTo(newStickerFile)
            check(newStickerFile.exists())

            searchRepo.requestDeleteStickerWithTagsDetail(deleteUuids).collect()
            addRepo.requestAddStickerWithTags(sticker, stickerUuidToUri(sticker.sticker.uuid))
                .collect()

            emit(Unit)
        }.flowOn(Dispatchers.IO)
    }
}