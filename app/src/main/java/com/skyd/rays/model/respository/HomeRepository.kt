package com.skyd.rays.model.respository

import com.skyd.rays.base.BaseRepository
import com.skyd.rays.model.bean.StickerWithTags
import com.skyd.rays.model.bean.TagBean
import com.skyd.rays.model.db.dao.TagDao
import com.skyd.rays.model.db.dao.sticker.StickerDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class HomeRepository @Inject constructor(
    private val stickerDao: StickerDao,
    private val tagDao: TagDao
) : BaseRepository() {
    fun requestRecommendTags(): Flow<List<TagBean>> {
        return tagDao.getRecommendTagsList(count = 10).distinctUntilChanged().flowOn(Dispatchers.IO)
    }

    fun requestRandomTags(): Flow<List<TagBean>> {
        return tagDao.getRandomTagsList(count = 10).distinctUntilChanged().flowOn(Dispatchers.IO)
    }

    fun requestRecentCreateStickers(): Flow<List<StickerWithTags>> {
        return stickerDao.getRecentCreateStickersList(count = 10)
            .distinctUntilChanged()
            .flowOn(Dispatchers.IO)
    }

    fun requestMostSharedStickers(): Flow<List<StickerWithTags>> {
        return stickerDao.getMostSharedStickersList(count = 10)
            .distinctUntilChanged()
            .flowOn(Dispatchers.IO)
    }
}