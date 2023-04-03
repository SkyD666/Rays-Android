package com.skyd.rays.util

import android.net.Uri
import com.skyd.rays.config.STICKER_DIR
import com.skyd.rays.model.bean.StickerWithTags
import java.io.File
import java.math.BigInteger
import java.security.MessageDigest

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