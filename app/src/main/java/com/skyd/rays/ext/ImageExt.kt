package com.skyd.rays.ext

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ColorSpace
import android.graphics.ImageDecoder
import android.graphics.ImageDecoder.OnHeaderDecodedListener
import android.net.Uri
import android.os.Build
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