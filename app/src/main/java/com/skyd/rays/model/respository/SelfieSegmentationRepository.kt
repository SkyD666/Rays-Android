package com.skyd.rays.model.respository

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.net.Uri
import androidx.core.graphics.get
import androidx.core.graphics.set
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.Segmentation
import com.google.mlkit.vision.segmentation.selfie.SelfieSegmenterOptions
import com.skyd.rays.appContext
import com.skyd.rays.base.BaseData
import com.skyd.rays.base.BaseRepository
import com.skyd.rays.ext.cropTransparency
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resumeWithException


class SelfieSegmentationRepository @Inject constructor() : BaseRepository() {

    suspend fun requestExport(
        foregroundBitmap: Bitmap,
        backgroundUri: Uri?,
        foregroundRect: RectF,
    ): Flow<BaseData<Bitmap>> {
        return flow {
            val resultBitmap: Bitmap = if (backgroundUri != null) {
                val underlayBitmap =
                    appContext.contentResolver.openInputStream(backgroundUri)!!.use {
                        val origin = BitmapFactory.decodeStream(it)
                        origin.copy(origin.config, true).apply {
                            setHasAlpha(true)
                            origin.recycle()
                        }
                    }

                val paint = Paint(Paint.FILTER_BITMAP_FLAG)
                val result = Canvas(underlayBitmap)
                foregroundRect.set(
                    foregroundRect.left * underlayBitmap.width,
                    foregroundRect.top * underlayBitmap.height,
                    foregroundRect.right * underlayBitmap.width,
                    foregroundRect.bottom * underlayBitmap.height,
                )
                result.drawBitmap(foregroundBitmap, null, foregroundRect, paint)
                underlayBitmap
            } else {
                foregroundBitmap
            }
            emitBaseData(BaseData<Bitmap>().apply {
                code = 0
                data = resultBitmap
            })
        }
    }

    suspend fun requestSelfieSegment(foregroundUri: Uri): Flow<BaseData<Pair<Bitmap, Long>>> {
        return flow {
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
            val foregroundBitmap = appContext.contentResolver.openInputStream(foregroundUri)!!.use {
                val origin = BitmapFactory.decodeStream(it)
                origin.copy(origin.config, true).apply {
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
            checkBaseData(BaseData<Pair<Bitmap, Long>>().apply {
                code = 0
                data = (foregroundBitmap.cropTransparency() ?: foregroundBitmap) to 0L
            })
        }
    }
}