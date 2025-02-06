package com.skyd.rays.model.respository

import android.graphics.Bitmap
import android.net.Uri
import com.skyd.rays.base.BaseRepository
import com.skyd.rays.ext.toBitmap
import com.skyd.rays.util.StyleTransferUtil
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class StyleTransferRepository @Inject constructor() : BaseRepository() {
    fun requestTransferredImage(
        style: Uri,
        content: Uri
    ): Flow<Pair<Bitmap, Long>> {
        return flowOnIo {
            val styleTransferUtil = StyleTransferUtil()
            styleTransferUtil.setStyleImage(style = style.toBitmap())
            emit(styleTransferUtil.transfer(image = content.toBitmap()))
        }
    }
}