package com.skyd.rays.model.db.dao.sticker

import android.os.Parcelable
import com.skyd.rays.R
import com.skyd.rays.appContext
import com.skyd.rays.config.STICKER_DIR
import com.skyd.rays.model.bean.StickerWithTags
import com.skyd.rays.model.db.dao.TagDao
import kotlinx.parcelize.Parcelize
import java.io.File
import java.util.UUID


@Parcelize
sealed interface HandleImportedStickerStrategy : Parcelable {
    fun handle(
        stickerDao: StickerDao,
        tagDao: TagDao,
        importedStickerWithTags: StickerWithTags,
        stickerFile: File,
    ): Boolean

    fun checkStickerWithTagsFormat(stickerWithTags: StickerWithTags) {
        check(stickerWithTags.sticker.stickerMd5.isNotBlank()) { "sticker's md5 is blank!" }
        UUID.fromString(stickerWithTags.sticker.uuid)
    }

    fun moveFile(stickerFile: File) {
        val destStickerFile = File(appContext.STICKER_DIR, stickerFile.name)
        stickerFile.copyTo(target = destStickerFile, overwrite = true)
        stickerFile.deleteRecursively()
    }

    val displayName: String

    // 冲突则跳过
    @Parcelize
    data object SkipStrategy : HandleImportedStickerStrategy {
        override fun handle(
            stickerDao: StickerDao,
            tagDao: TagDao,
            importedStickerWithTags: StickerWithTags,
            stickerFile: File,
        ): Boolean {
            checkStickerWithTagsFormat(importedStickerWithTags)
            val stickerUuid = importedStickerWithTags.sticker.uuid
            // 冲突跳过
            if (stickerDao.containsByUuid(stickerUuid) != 0) {
                return false
            }
            moveFile(stickerFile)
            stickerDao.innerAddSticker(importedStickerWithTags.sticker)
            importedStickerWithTags.tags.forEach {
                it.stickerUuid = stickerUuid
            }
            tagDao.deleteTags(stickerUuid)
            tagDao.addTags(importedStickerWithTags.tags)
            return true
        }

        override val displayName: String
            get() = appContext.getString(R.string.handle_imported_sticker_proxy_skip)
    }

    // 冲突则覆盖
    @Parcelize
    data object ReplaceStrategy : HandleImportedStickerStrategy {
        override fun handle(
            stickerDao: StickerDao,
            tagDao: TagDao,
            importedStickerWithTags: StickerWithTags,
            stickerFile: File,
        ): Boolean {
            checkStickerWithTagsFormat(importedStickerWithTags)
            val stickerUuid = importedStickerWithTags.sticker.uuid
            moveFile(stickerFile)
            stickerDao.innerAddSticker(importedStickerWithTags.sticker)
            importedStickerWithTags.tags.forEach {
                it.stickerUuid = stickerUuid
            }
            tagDao.deleteTags(stickerUuid)
            tagDao.addTags(importedStickerWithTags.tags)
            return true
        }

        override val displayName: String
            get() = appContext.getString(R.string.handle_imported_sticker_proxy_replace)
    }
}