package com.skyd.rays.ext

import android.net.Uri
import com.skyd.rays.appContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException


fun Uri.copyTo(target: File): File {
    return appContext.contentResolver.openInputStream(this)!!.use { it.saveTo(target) }
}

fun InputStream.saveTo(target: File): File {
    target.parentFile?.takeIf { !it.exists() }?.mkdirs()
    if (!target.exists()) {
        target.createNewFile()
    }
    FileOutputStream(target).use { copyTo(it) }
    return target
}

fun File.md5(): String? = FileInputStream(this).use { fis -> fis.md5() }

fun InputStream.md5(): String? {
    var bi: BigInteger? = null
    try {
        val buffer = ByteArray(4096)
        var len: Int
        val md = MessageDigest.getInstance("MD5")
        while (read(buffer).also { len = it } != -1) {
            md.update(buffer, 0, len)
        }
        val b = md.digest()
        bi = BigInteger(1, b)
    } catch (e: NoSuchAlgorithmException) {
        e.printStackTrace()
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return bi?.toString(16)
}

inline val String.extName: String
    get() = substringAfterLast(".", missingDelimiterValue = "")