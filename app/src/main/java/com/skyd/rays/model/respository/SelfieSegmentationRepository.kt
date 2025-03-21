package com.skyd.rays.model.respository

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.net.Uri
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import androidx.core.graphics.createBitmap
import androidx.core.graphics.get
import androidx.core.graphics.set
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.Segmentation
import com.google.mlkit.vision.segmentation.selfie.SelfieSegmenterOptions
import com.skyd.rays.appContext
import com.skyd.rays.base.BaseRepository
import com.skyd.rays.ext.cropTransparency
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resumeWithException


class SelfieSegmentationRepository @Inject constructor() : BaseRepository() {
    fun requestExport(
        foregroundBitmap: Bitmap,
        backgroundUri: Uri?,
        backgroundSize: IntSize,
        foregroundScale: Float,
        foregroundOffset: Offset,
        foregroundRotation: Float,
        foregroundSize: IntSize,
        borderSize: IntSize,
    ): Flow<Bitmap> = flow {
        // 在Compose里显示的大小和真实Bitmap大小的比率
        val foregroundRatio = foregroundSize.width.toFloat() / foregroundBitmap.width
        val backgroundRatio: Float

        val underlayBitmap = if (backgroundUri != null) {
            appContext.contentResolver.openInputStream(backgroundUri)!!.use {
                val origin = BitmapFactory.decodeStream(it)
                origin.copy(origin.config!!, true).apply {
                    setHasAlpha(true)
                    origin.recycle()
                }
            }.also { backgroundRatio = backgroundSize.width.toFloat() / it.width }
        } else {
            createBitmap(borderSize.width, borderSize.height).also {
                /* 没有背景时用透明图像，比率是1 */
                backgroundRatio = 1f
            }
        }

        val matrix = Matrix()
        with(matrix) {
            // 计算上述两个比率的差异
            val scale1 = foregroundRatio / backgroundRatio
            // 水平竖直移动
            val dx = foregroundOffset.x / scale1 / backgroundRatio
            val dy = foregroundOffset.y / scale1 / backgroundRatio
            setTranslate(dx, dy)

            // 是调整编辑页面和实际位图的大小差异用的
            postScale(scale1, scale1)

            // 计算“调整编辑页面和实际位图的大小差异”后的中心点
            val centerX = ((foregroundBitmap.width + 2 * dx) / 2f) * scale1
            val centerY = ((foregroundBitmap.height + 2 * dy) / 2f) * scale1
            // 是用户主动缩放的大小
            postScale(foregroundScale, foregroundScale, centerX, centerY)

            // 由于上一行就是在中心点进行缩放的，因此缩放后，图像的中心点没变，下面旋转继续用这个中心点
            postRotate(foregroundRotation, centerX, centerY)
        }
        Canvas(underlayBitmap).drawBitmap(
            foregroundBitmap,
            matrix,
            Paint(Paint.FILTER_BITMAP_FLAG).apply { isAntiAlias = true }
        )
        emit(underlayBitmap)
    }.flowOn(Dispatchers.IO)

    fun requestSelfieSegment(foregroundUri: Uri): Flow<Bitmap> = flow {
        emit(suspendCancellableCoroutine { cont ->
            val options = SelfieSegmenterOptions.Builder()
                .setDetectorMode(SelfieSegmenterOptions.SINGLE_IMAGE_MODE)
                .build()
            val image: InputImage = InputImage.fromFilePath(appContext, foregroundUri)
            Segmentation.getClient(options).process(image)
                .addOnSuccessListener { segmentationMask ->
                    cont.resume(value = segmentationMask, onCancellation = null)
                }.addOnFailureListener { e -> cont.resumeWithException(e) }
        })
    }.map { segmentationMask ->
        val foregroundBitmap =
            appContext.contentResolver.openInputStream(foregroundUri)!!.use {
                val origin = BitmapFactory.decodeStream(it)
                origin.copy(origin.config!!, true).apply {
                    setHasAlpha(true)
                    origin.recycle()
                }
            }

        val mask = segmentationMask.buffer
        val maskWidth = segmentationMask.width
        val maskHeight = segmentationMask.height

        for (y in 0 until maskHeight) {
            for (x in 0 until maskWidth) {
                val confidence = mask.float
                if (confidence in 0.0f..<0.6f) {
                    foregroundBitmap[x, y] = 0x00000000
                } else if (confidence in 0.6f..<0.9f) {
                    val color = foregroundBitmap[x, y]
                    val alpha = (color ushr 24) * (confidence - 0.6f) * 3.333f
                    foregroundBitmap[x, y] =
                        (color and 0x00FFFFFF) + (alpha.toInt() shl 24)
                }
            }
        }
        foregroundBitmap.cropTransparency() ?: foregroundBitmap
    }.flowOn(Dispatchers.IO)
}