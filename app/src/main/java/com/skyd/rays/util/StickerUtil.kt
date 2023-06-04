package com.skyd.rays.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.skyd.rays.R
import com.skyd.rays.appContext
import com.skyd.rays.config.STICKER_DIR
import com.skyd.rays.ext.dataStore
import com.skyd.rays.ext.get
import com.skyd.rays.model.bean.StickerWithTags
import com.skyd.rays.model.db.AppDatabase
import com.skyd.rays.model.preference.share.StickerExtNamePreference
import com.skyd.rays.model.preference.share.UriStringSharePreference
import com.skyd.rays.util.image.ImageFormatChecker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.math.BigInteger
import java.security.MessageDigest
import kotlin.random.Random

private val scope = CoroutineScope(Dispatchers.IO)

fun Context.sendSticker(uuid: String, onSuccess: (() -> Unit)? = null) {
    scope.launch(Dispatchers.IO) {
        val stickerFile = externalStickerUuidToFile(uuid)
        val contentUri = FileProvider.getUriForFile(
            this@sendSticker,
            "${packageName}.fileprovider", stickerFile
        )

        withContext(Dispatchers.Main) {
            val shareIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                putExtra(Intent.EXTRA_STREAM, contentUri)
                type = "image/*"
            }
            startActivity(
                Intent.createChooser(
                    shareIntent,
                    resources.getText(R.string.send_sticker)
                )
            )
        }

        with(AppDatabase.getInstance(this@sendSticker)) {
            if (dataStore.get(UriStringSharePreference.key) == true) {
                contentUri.shareStickerUriString(
                    context = this@sendSticker,
                    packages = uriStringSharePackageDao().getAllPackage().map { it.packageName }
                )
            }
            stickerDao().addShareCount(uuid = uuid)
        }

        withContext(Dispatchers.IO) {
            onSuccess?.invoke()
        }
    }
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

/**
 * 针对外部应用
 */
fun externalStickerUuidToFile(uuid: String): File {
    val originFile = stickerUuidToFile(uuid)
    val outputDir = File(appContext.cacheDir, "TempSticker")
    if (appContext.dataStore.get(StickerExtNamePreference.key) == false ||
        (!outputDir.exists() && !outputDir.mkdirs())
    ) {
        return originFile
    }
    val extensionName = originFile.inputStream().use {
        ImageFormatChecker.check(it).toString()
    }
    val resultFileName = uuid + "_" + Random.nextInt(0, Int.MAX_VALUE) + extensionName
    val resultFile = originFile.copyTo(
        target = File(outputDir, resultFileName),
        overwrite = true
    )
    scope.launch {
        // > 5MB
        if (outputDir.walkTopDown().filter { it.isFile }.map { it.length() }.sum() > 5_242_880) {
            outputDir.walkBottomUp().fold(true) { res, it ->
                (it.name == resultFileName || it.delete() || !it.exists()) && res
            }
        }
    }
    return resultFile
}

/**
 * 针对内部操作，针对原图片本身
 */
fun stickerUuidToFile(uuid: String): File = File(STICKER_DIR, uuid)

fun externalStickerUuidToUri(uuid: String) = Uri.fromFile(externalStickerUuidToFile(uuid))

fun stickerUuidToUri(uuid: String) = Uri.fromFile(stickerUuidToFile(uuid))

/**
 * 微信聊天框输入图片 uri 自动识别图片的功能
 */
private fun Uri.shareStickerUriString(context: Context, packages: List<String>) {
    packages.forEach {
        context.grantUriPermission(it, this, Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    val clipboard = ContextCompat.getSystemService(context, ClipboardManager::class.java)
    clipboard?.setPrimaryClip(ClipData.newPlainText("Share sticker", this.toString()))
}