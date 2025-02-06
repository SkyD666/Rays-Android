package com.skyd.rays.util.image

import com.skyd.rays.appContext
import com.skyd.rays.model.db.dao.sticker.MimeTypeDao
import com.skyd.rays.util.image.format.FormatStandard.Companion.formatStandards
import com.skyd.rays.util.image.format.ImageFormat
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.runBlocking
import java.io.InputStream

object ImageFormatChecker {
    private suspend fun check(stickerUuid: String?): ImageFormat? {
        stickerUuid ?: return null
        val mimeType = hiltEntryPoint.mimeTypeDao.getMimeTypeOrNull(stickerUuid)
        return if (mimeType == null) null else ImageFormat.fromMimeType(mimeType)
    }

    fun check(tested: InputStream, stickerUuid: String? = null): ImageFormat = runBlocking {
        check(stickerUuid = stickerUuid)?.let { return@runBlocking it }

        var readByteArray: ByteArray? = null
        formatStandards.forEach {
            val result = it.check(tested, readByteArray)
            readByteArray = result.second
            if (result.first) {
                if (!stickerUuid.isNullOrBlank()) saveMimeType(it.format, stickerUuid)
                return@runBlocking it.format
            }
        }
        ImageFormat.UNDEFINED
    }

    fun check(tested: ByteArray, stickerUuid: String? = null): ImageFormat = runBlocking {
        check(stickerUuid = stickerUuid)?.let { return@runBlocking it }

        formatStandards.forEach {
            if (it.check(tested)) {
                if (!stickerUuid.isNullOrBlank()) saveMimeType(it.format, stickerUuid)
                return@runBlocking it.format
            }
        }
        ImageFormat.UNDEFINED
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface ImageFormatEntryPoint {
        val mimeTypeDao: MimeTypeDao
    }

    private val hiltEntryPoint =
        EntryPointAccessors.fromApplication(appContext, ImageFormatEntryPoint::class.java)

    suspend fun saveMimeType(format: ImageFormat, stickerUuid: String) = runCatching {
        with(hiltEntryPoint.mimeTypeDao) {
            if (getMd5(stickerUuid = stickerUuid) != null) {
                setMimeType(stickerUuid = stickerUuid, mimeType = format.toMimeType())
            }
        }
    }.onFailure {
        it.printStackTrace()
    }
}