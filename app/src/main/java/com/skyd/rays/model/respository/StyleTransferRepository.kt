package com.skyd.rays.model.respository

import android.graphics.Bitmap
import android.net.Uri
import com.skyd.rays.base.BaseRepository
import com.skyd.rays.ext.toBitmap
import com.skyd.rays.util.StyleTransferUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class StyleTransferRepository @Inject constructor() : BaseRepository() {
    fun requestTransferredImage(
        style: Uri,
        content: Uri
    ): Flow<Pair<Bitmap, Long>> = flow {
        val styleTransferUtil = StyleTransferUtil()
        styleTransferUtil.setStyleImage(style = style.toBitmap())
        emit(styleTransferUtil.transfer(image = content.toBitmap()))
    }.flowOn(Dispatchers.IO)
}