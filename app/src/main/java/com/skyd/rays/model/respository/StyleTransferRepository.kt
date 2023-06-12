package com.skyd.rays.model.respository

import android.graphics.Bitmap
import com.skyd.rays.base.BaseData
import com.skyd.rays.base.BaseRepository
import com.skyd.rays.util.StyleTransferUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class StyleTransferRepository @Inject constructor() : BaseRepository() {

    suspend fun requestTransferredImage(
        style: Bitmap,
        content: Bitmap
    ): Flow<BaseData<Pair<Bitmap, Long>>> {
        return flow {
            val styleTransferUtil = StyleTransferUtil()
            styleTransferUtil.setStyleImage(style = style)
            emitBaseData(BaseData<Pair<Bitmap, Long>>().apply {
                code = 0
                data = styleTransferUtil.transfer(image = content)
            })
        }
    }
}