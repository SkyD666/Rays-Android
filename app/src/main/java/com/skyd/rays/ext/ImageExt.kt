package com.skyd.rays.ext

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.ui.layout.ContentScale
import com.skyd.rays.R
import com.skyd.rays.appContext


fun ContentScale.toDisplayName(): String = when (this) {
    ContentScale.Crop -> appContext.getString(R.string.image_scale_crop)
    ContentScale.Inside -> appContext.getString(R.string.image_scale_inside)
    ContentScale.FillWidth -> appContext.getString(R.string.image_scale_fill_width)
    ContentScale.FillHeight -> appContext.getString(R.string.image_scale_fill_height)
    ContentScale.Fit -> appContext.getString(R.string.image_scale_fit)
    ContentScale.FillBounds -> appContext.getString(R.string.image_scale_fill_bounds)
    else -> ""
}

fun Uri.toBitmap(): Bitmap {
    return appContext.contentResolver.openInputStream(this)!!.use {
        val op = BitmapFactory.Options()
        op.inPreferredConfig = Bitmap.Config.ARGB_8888
        BitmapFactory.decodeStream(it, null, op)!!
    }
}

fun Bitmap.cropTransparency(): Bitmap? {
    var minX = width
    var minY = height
    var maxX = -1
    var maxY = -1
    for (y in 0 until height) {
        for (x in 0 until width) {
            val alpha = getPixel(x, y) shr 24 and 255
            // pixel is not 100% transparent
            if (alpha > 0) {
                if (x < minX) minX = x
                if (x > maxX) maxX = x
                if (y < minY) minY = y
                if (y > maxY) maxY = y
            }
        }
    }
    return if (maxX < minX || maxY < minY) null else Bitmap.createBitmap(
        this,
        minX,
        minY,
        maxX - minX + 1,
        maxY - minY + 1
    )
}