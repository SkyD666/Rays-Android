package com.skyd.rays.model.db.dao

import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery
import com.skyd.rays.appContext
import com.skyd.rays.config.STICKER_DIR
import com.skyd.rays.ext.dataStore
import com.skyd.rays.ext.get
import com.skyd.rays.model.bean.STICKER_TABLE_NAME
import com.skyd.rays.model.bean.StickerBean
import com.skyd.rays.model.bean.StickerBean.Companion.STICKER_MD5_COLUMN
import com.skyd.rays.model.bean.StickerBean.Companion.UUID_COLUMN
import com.skyd.rays.model.bean.StickerWithTags
import com.skyd.rays.model.preference.CurrentStickerUuidPreference
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.io.File
import java.util.*

@Dao
interface StickerDao {
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface StickerDaoEntryPoint {
        val tagDao: TagDao
    }

    @Transaction
    @RawQuery
    fun getStickerWithTagsList(sql: SupportSQLiteQuery): List<StickerWithTags>

    @Transaction
    @Query("SELECT * FROM $STICKER_TABLE_NAME")
    fun getAllStickerWithTagsList(): List<StickerWithTags>

    @Transaction
    @Query("SELECT * FROM $STICKER_TABLE_NAME")
    fun getStickerList(): List<StickerBean>

    @Transaction
    @Query("SELECT * FROM $STICKER_TABLE_NAME WHERE $UUID_COLUMN LIKE :stickerUuid")
    fun getStickerWithTags(stickerUuid: String): StickerWithTags?

    @Transaction
    @Query("SELECT $UUID_COLUMN FROM $STICKER_TABLE_NAME WHERE $STICKER_MD5_COLUMN LIKE :stickerMd5")
    fun containsByMd5(stickerMd5: String): String?

    @Transaction
    @Query("SELECT COUNT(*) FROM $STICKER_TABLE_NAME WHERE $UUID_COLUMN LIKE :uuid")
    fun containsByUuid(uuid: String): Int

    @Transaction
    fun addStickerWithTags(
        stickerWithTags: StickerWithTags,
        updateModifyTime: Boolean = true
    ): String {
        check(stickerWithTags.sticker.stickerMd5.isNotBlank()) { "sticker's md5 is blank!" }
        if (updateModifyTime) {
            stickerWithTags.sticker.modifyTime = System.currentTimeMillis()
        }
        val hiltEntryPoint =
            EntryPointAccessors.fromApplication(appContext, StickerDaoEntryPoint::class.java)
        var stickerUuid = stickerWithTags.sticker.uuid
        runCatching {
            UUID.fromString(stickerUuid)
        }.onFailure {
            stickerUuid = UUID.randomUUID().toString()
            stickerWithTags.sticker.uuid = stickerUuid
        }
        innerAddSticker(stickerWithTags.sticker)
        stickerWithTags.tags.forEach {
            it.stickerUuid = stickerUuid
        }
        hiltEntryPoint.tagDao.apply {
            deleteTags(stickerUuid)
            addTags(stickerWithTags.tags)
        }
        return stickerUuid
    }

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun innerAddSticker(stickerBean: StickerBean)

    @Transaction
    fun deleteStickerWithTags(stickerUuid: String): Int {
        val scope = CoroutineScope(Dispatchers.IO)
        val currentStickerUuid = appContext.dataStore.get(CurrentStickerUuidPreference.key)
        if (currentStickerUuid == stickerUuid) {
            CurrentStickerUuidPreference.put(
                appContext, scope, CurrentStickerUuidPreference.default
            )
        }
        File(STICKER_DIR, stickerUuid).deleteRecursively()
        // 设置了外键，ForeignKey.CASCADE，因此会自动deleteTags
        return innerDeleteSticker(stickerUuid)
    }

    @Transaction
    fun deleteAllStickerWithTags() {
        val scope = CoroutineScope(Dispatchers.IO)
        CurrentStickerUuidPreference.put(appContext, scope, CurrentStickerUuidPreference.default)
        File(STICKER_DIR).deleteRecursively()
        // 设置了外键，ForeignKey.CASCADE，因此会自动deleteTags
        innerDeleteAllStickers()
    }

    @Transaction
    @Query("DELETE FROM $STICKER_TABLE_NAME WHERE $UUID_COLUMN LIKE :stickerUuid")
    fun innerDeleteSticker(stickerUuid: String): Int

    @Transaction
    fun webDavImportData(stickerWithTagsList: List<StickerWithTags>) {
        stickerWithTagsList.forEach {
            addStickerWithTags(stickerWithTags = it, updateModifyTime = false)
        }
    }

    @Transaction
    @Query("DELETE FROM $STICKER_TABLE_NAME")
    fun innerDeleteAllStickers()
}