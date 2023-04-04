package com.skyd.rays.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.skyd.rays.R
import com.skyd.rays.config.STICKER_DIR
import com.skyd.rays.model.bean.StickerWithTags
import java.io.File
import java.math.BigInteger
import java.security.MessageDigest


fun Context.sendSticker(uuid: String) {
    val contentUri = FileProvider
        .getUriForFile(this, "${packageName}.fileprovider", stickerUuidToFile(uuid))
    val shareIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        putExtra(Intent.EXTRA_STREAM, contentUri)
        type = "image/*"
    }
    startActivity(Intent.createChooser(shareIntent, resources.getText(R.string.send_sticker)))
}

fun StickerWithTags.md5(): String {
    val summary = StringBuilder("$sticker\n")
    tags.forEach {
        summary.append(it).append("\n")
    }
    val md = MessageDigest.getInstance("MD5")
    return BigInteger(1, md.digest(summary.toString().toByteArray()))
        .toString(16).padStart(32, '0')
}

fun stickerUuidToFile(uuid: String) = File(STICKER_DIR, uuid)

fun stickerUuidToUri(uuid: String) = Uri.fromFile(stickerUuidToFile(uuid))