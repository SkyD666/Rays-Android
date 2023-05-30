package com.skyd.rays.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.skyd.rays.R
import com.skyd.rays.config.STICKER_DIR
import com.skyd.rays.model.bean.StickerWithTags
import com.skyd.rays.model.db.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.math.BigInteger
import java.security.MessageDigest

private val scope = CoroutineScope(Dispatchers.IO)

fun Context.sendSticker(uuid: String, onSuccess: (() -> Unit)? = null) {
    val contentUri = FileProvider
        .getUriForFile(this, "${packageName}.fileprovider", stickerUuidToFile(uuid))
    val shareIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        putExtra(Intent.EXTRA_STREAM, contentUri)
        type = "image/*"
    }
    contentUri.wechatStickerUriString(context = this)
    startActivity(Intent.createChooser(shareIntent, resources.getText(R.string.send_sticker)))
    scope.launch {
        AppDatabase.getInstance(this@sendSticker).stickerDao().addShareCount(uuid = uuid)
    }
    onSuccess?.invoke()
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

/**
 * 微信聊天框输入图片 uri 自动识别图片的功能
 */
private fun Uri.wechatStickerUriString(context: Context) {
    context.grantUriPermission(
        "com.tencent.mm", this, Intent.FLAG_GRANT_READ_URI_PERMISSION
    )
    val clipboard = ContextCompat.getSystemService(context, ClipboardManager::class.java)
    clipboard?.setPrimaryClip(ClipData.newPlainText("Share sticker", this.toString()))
}