package com.skyd.rays.model.db.dao.sticker

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.MapColumn
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Transaction
import androidx.sqlite.db.SupportSQLiteQuery
import com.skyd.rays.appContext
import com.skyd.rays.config.STICKER_DIR
import com.skyd.rays.di.get
import com.skyd.rays.ext.dataStore
import com.skyd.rays.ext.getOrDefault
import com.skyd.rays.ext.safeDbVariableNumber
import com.skyd.rays.model.bean.STICKER_SHARE_TIME_TABLE_NAME
import com.skyd.rays.model.bean.STICKER_TABLE_NAME
import com.skyd.rays.model.bean.StickerBean
import com.skyd.rays.model.bean.StickerBean.Companion.CLICK_COUNT_COLUMN
import com.skyd.rays.model.bean.StickerBean.Companion.CREATE_TIME_COLUMN
import com.skyd.rays.model.bean.StickerBean.Companion.MODIFY_TIME_COLUMN
import com.skyd.rays.model.bean.StickerBean.Companion.SHARE_COUNT_COLUMN
import com.skyd.rays.model.bean.StickerBean.Companion.STICKER_MD5_COLUMN
import com.skyd.rays.model.bean.StickerBean.Companion.UUID_COLUMN
import com.skyd.rays.model.bean.StickerShareTimeBean
import com.skyd.rays.model.bean.StickerWithTags
import com.skyd.rays.model.bean.StickerWithTagsAndFile
import com.skyd.rays.model.bean.TagBean
import com.skyd.rays.model.db.dao.TagDao
import com.skyd.rays.model.db.dao.cache.StickerShareTimeDao
import com.skyd.rays.model.db.objectbox.entity.StickerEmbedding
import com.skyd.rays.model.db.objectbox.entity.StickerEmbedding_
import com.skyd.rays.model.preference.CurrentStickerUuidPreference
import com.skyd.rays.util.stickerUuidToFile
import io.objectbox.BoxStore
import io.objectbox.query.QueryBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import java.io.File
import java.util.UUID

@Dao
interface StickerDao {
    @Transaction
    @RawQuery(observedEntities = [StickerBean::class, TagBean::class])
    fun getStickerWithTagsList(sql: SupportSQLiteQuery): Flow<List<StickerWithTags>>

    @Transaction
    @RawQuery(observedEntities = [StickerBean::class, TagBean::class])
    fun getStickerWithTagsPaging(sql: SupportSQLiteQuery): PagingSource<Int, StickerWithTags>

    @Transaction
    @RawQuery(observedEntities = [StickerBean::class, TagBean::class])
    suspend fun getStickerUuidList(sql: SupportSQLiteQuery): List<String>

    @Transaction
    @Query("SELECT $UUID_COLUMN FROM $STICKER_TABLE_NAME")
    suspend fun getAllStickerUuidList(): List<String>

    @Transaction
    @Query("SELECT * FROM $STICKER_TABLE_NAME")
    suspend fun getAllStickerWithTagsList(): List<StickerWithTags>

    @Transaction
    @Query("SELECT * FROM $STICKER_TABLE_NAME WHERE $UUID_COLUMN IN (:uuids)")
    fun getAllStickerWithTagsList(uuids: Collection<String>): Flow<List<StickerWithTags>>

    @Transaction
    @Query("SELECT * FROM $STICKER_TABLE_NAME")
    suspend fun getStickerList(): List<StickerBean>

    @Transaction
    @Query("SELECT * FROM $STICKER_TABLE_NAME WHERE $UUID_COLUMN LIKE :stickerUuid")
    suspend fun getStickerWithTags(stickerUuid: String): StickerWithTags?

    @Transaction
    @Query("SELECT * FROM $STICKER_TABLE_NAME ORDER BY $MODIFY_TIME_COLUMN DESC LIMIT :count")
    suspend fun getRecentModifiedStickers(count: Int = 15): List<StickerWithTags>

    @Transaction
    @Query(
        """
        SELECT $UUID_COLUMN, $MODIFY_TIME_COLUMN FROM $STICKER_TABLE_NAME
        WHERE $UUID_COLUMN IN (:stickerUuids)
        """
    )
    suspend fun getStickerModified(stickerUuids: List<String>): Map<
            @MapColumn(columnName = UUID_COLUMN) String,
            @MapColumn(columnName = MODIFY_TIME_COLUMN) Long>

    @Transaction
    @Query(
        """
        SELECT $UUID_COLUMN, ${StickerBean.TITLE_COLUMN} FROM $STICKER_TABLE_NAME
        WHERE $UUID_COLUMN IN (:stickerUuids)
        """
    )
    suspend fun getStickerTitles(stickerUuids: List<String>): Map<
            @MapColumn(columnName = UUID_COLUMN) String,
            @MapColumn(columnName = StickerBean.TITLE_COLUMN) String>

    @Transaction
    @Query("SELECT * FROM $STICKER_TABLE_NAME WHERE $UUID_COLUMN LIKE :stickerUuid")
    fun getStickerWithTagsFlow(stickerUuid: String): Flow<StickerWithTags?>

    @Transaction
    @Query(
        """SELECT *
        FROM $STICKER_TABLE_NAME
        ORDER BY $CREATE_TIME_COLUMN DESC
        LIMIT :count
        """
    )
    fun getRecentCreateStickersList(count: Int): Flow<List<StickerWithTags>>

    @Transaction
    @Query(
        """
        SELECT *
        FROM $STICKER_TABLE_NAME
        ORDER BY $SHARE_COUNT_COLUMN DESC
        LIMIT :count
        """
    )
    fun getMostSharedStickersList(count: Int): Flow<List<StickerWithTags>>

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query(
        """
        SELECT * FROM $STICKER_TABLE_NAME LEFT JOIN (
            SELECT *, MAX(${StickerShareTimeBean.SHARE_TIME_COLUMN})
            FROM $STICKER_SHARE_TIME_TABLE_NAME
            GROUP BY ${StickerShareTimeBean.STICKER_UUID_COLUMN}
            ORDER BY ${StickerShareTimeBean.SHARE_TIME_COLUMN} DESC
            LIMIT :count
        ) AS shareTime
        WHERE $UUID_COLUMN = shareTime.${StickerShareTimeBean.STICKER_UUID_COLUMN}
        ORDER BY ${StickerShareTimeBean.SHARE_TIME_COLUMN} DESC
        """
    )
    fun getRecentSharedStickers(count: Int): Flow<List<StickerWithTags>>

    @Transaction
    @Query("SELECT $UUID_COLUMN FROM $STICKER_TABLE_NAME WHERE $STICKER_MD5_COLUMN LIKE :stickerMd5")
    suspend fun containsByMd5(stickerMd5: String): String?

    @Transaction
    @Query("SELECT COUNT(*) FROM $STICKER_TABLE_NAME WHERE $UUID_COLUMN LIKE :uuid")
    suspend fun containsByUuid(uuid: String): Int

    @Transaction
    @Query(
        """UPDATE $STICKER_TABLE_NAME
           SET $CLICK_COUNT_COLUMN = $CLICK_COUNT_COLUMN + :count
           WHERE $UUID_COLUMN = :uuid"""
    )
    suspend fun addClickCount(uuid: String, count: Int = 1): Int

    @Transaction
    @Query(
        """UPDATE $STICKER_TABLE_NAME
           SET $SHARE_COUNT_COLUMN = $SHARE_COUNT_COLUMN + :count
           WHERE $UUID_COLUMN IN (:uuids)"""
    )
    suspend fun addShareCount(uuids: Collection<String>, count: Int = 1): Int

    @Transaction
    suspend fun shareStickers(uuids: Collection<String>, count: Int = 1) {
        val currentTimeMillis = System.currentTimeMillis()
        uuids.toList().safeDbVariableNumber { addShareCount(it, count) }
        get<StickerShareTimeDao>().updateShareTime(uuids.map { stickerUuid ->
            StickerShareTimeBean(stickerUuid, currentTimeMillis)
        })
    }

    @Transaction
    @Query("SELECT * FROM $STICKER_TABLE_NAME ORDER BY $SHARE_COUNT_COLUMN DESC LIMIT :count")
    fun getPopularStickersList(count: Int = 15): Flow<List<StickerWithTags>>

    @Transaction
    suspend fun addStickerWithTags(
        stickerWithTags: StickerWithTags,
        updateModifyTime: Boolean = true
    ): String {
        check(stickerWithTags.sticker.stickerMd5.isNotBlank()) { "sticker's md5 is blank!" }
        if (updateModifyTime) {
            stickerWithTags.sticker.modifyTime = System.currentTimeMillis()
        }
        var stickerUuid = stickerWithTags.sticker.uuid
        runCatching {
            UUID.fromString(stickerUuid)
        }.onFailure {
            stickerUuid = UUID.randomUUID().toString()
            stickerWithTags.sticker.uuid = stickerUuid
        }
        addSticker(stickerWithTags.sticker)
        stickerWithTags.tags.forEach {
            it.stickerUuid = stickerUuid
        }
        get<TagDao>().apply {
            deleteTags(stickerUuid)
            addTags(stickerWithTags.tags)
        }
        return stickerUuid
    }

    suspend fun addSticker(stickerBean: StickerBean) {
        get<BoxStore>().boxFor(StickerEmbedding::class.java).apply {
            val oldEmbedding = query().equal(
                StickerEmbedding_.uuid,
                stickerBean.uuid,
                QueryBuilder.StringOrder.CASE_SENSITIVE
            ).build().findUnique()
            if (oldEmbedding != null) {
                remove(oldEmbedding)
            }
        }
        _innerAddSticker(stickerBean)
    }

    @Suppress("FunctionName")
    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun _innerAddSticker(stickerBean: StickerBean)

    @Transaction
    suspend fun deleteStickerWithTags(stickerUuids: List<String>): Int {
        val scope = CoroutineScope(Dispatchers.IO)
        val currentStickerUuid = appContext.dataStore.getOrDefault(CurrentStickerUuidPreference)
        stickerUuids.forEach { stickerUuid ->
            if (currentStickerUuid == stickerUuid) {
                CurrentStickerUuidPreference.put(
                    appContext, scope, CurrentStickerUuidPreference.default
                )
            }
            stickerUuidToFile(stickerUuid).deleteRecursively()
        }
        // 设置了外键，ForeignKey.CASCADE，因此会自动deleteTags
        return innerDeleteStickers(stickerUuids)
    }

    @Transaction
    suspend fun deleteAllStickerWithTags() {
        val scope = CoroutineScope(Dispatchers.IO)
        CurrentStickerUuidPreference.put(appContext, scope, CurrentStickerUuidPreference.default)
        File(appContext.STICKER_DIR).deleteRecursively()
        // 设置了外键，ForeignKey.CASCADE，因此会自动deleteTags
        innerDeleteAllStickers()
    }

    @Transaction  //加上后会导致Flow更新不正常（搜索页面不能及时删除没有tag的表情包）
    @Query("DELETE FROM $STICKER_TABLE_NAME WHERE $UUID_COLUMN IN (:stickerUuids)")
    suspend fun innerDeleteStickers(stickerUuids: List<String>): Int

    @Transaction
    @Query("DELETE FROM $STICKER_TABLE_NAME WHERE $UUID_COLUMN LIKE :stickerUuid")
    suspend fun innerDeleteSticker(stickerUuid: String): Int

    @Transaction
    suspend fun importDataFromExternal(stickerWithTagsList: List<StickerWithTags>) {
        // 原始方案就是覆盖
        stickerWithTagsList.forEach {
            addStickerWithTags(stickerWithTags = it, updateModifyTime = false)
        }
    }

    @Transaction
    suspend fun importDataFromExternal(
        stickerWithTagsList: List<StickerWithTagsAndFile>,
        strategy: HandleImportedStickerStrategy,
    ): Int {
        var updatedCount = 0
        stickerWithTagsList.forEach {
            val currentTimeMillis = System.currentTimeMillis()
            it.stickerWithTags.sticker.apply {
                if (createTime == 0L) {
                    createTime = currentTimeMillis
                }
                if (modifyTime == 0L) {
                    modifyTime = currentTimeMillis
                }
            }
            val updated = strategy.handle(
                stickerDao = this,
                tagDao = get<TagDao>(),
                importedStickerWithTags = it.stickerWithTags,
                stickerFile = it.stickerFile,
            )
            if (updated) updatedCount++
        }
        return updatedCount
    }

    @Transaction
    @Query("DELETE FROM $STICKER_TABLE_NAME")
    suspend fun innerDeleteAllStickers()
}