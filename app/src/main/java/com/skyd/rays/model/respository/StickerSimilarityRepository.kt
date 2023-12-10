package com.skyd.rays.model.respository

import android.graphics.Bitmap
import com.skyd.rays.base.BaseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class StickerSimilarityRepository @Inject constructor() : BaseRepository() {

    suspend fun requestSimilarStickers(
        style: Bitmap,
        content: Bitmap
    ): Flow<Pair<Bitmap, Long>> {
        return flow {
            // Initialization


//            emit(styleTransferUtil.transfer(image = content))
        }
    }
}