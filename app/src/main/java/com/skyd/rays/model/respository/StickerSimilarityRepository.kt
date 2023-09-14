package com.skyd.rays.model.respository

import android.graphics.Bitmap
import com.skyd.rays.base.BaseData
import com.skyd.rays.base.BaseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.tensorflow.lite.task.processor.NearestNeighbor
import javax.inject.Inject

class StickerSimilarityRepository @Inject constructor() : BaseRepository() {

    suspend fun requestSimilarStickers(
        style: Bitmap,
        content: Bitmap
    ): Flow<BaseData<Pair<Bitmap, Long>>> {
        return flow {
            // Initialization


//            emitBaseData(BaseData<Pair<Bitmap, Long>>().apply {
//                code = 0
//                data = styleTransferUtil.transfer(image = content)
//            })
        }
    }
}