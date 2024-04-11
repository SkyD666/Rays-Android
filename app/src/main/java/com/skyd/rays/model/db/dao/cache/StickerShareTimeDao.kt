package com.skyd.rays.model.db.dao.cache

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.skyd.rays.model.bean.STICKER_SHARE_TIME_TABLE_NAME
import com.skyd.rays.model.bean.StickerShareTimeBean

@Dao
interface StickerShareTimeDao {
    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun updateShareTime(stickerShareTimeBean: StickerShareTimeBean)

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun updateShareTime(stickerShareTimeList: List<StickerShareTimeBean>)

    @Transaction
    @Query(
        """
        SELECT ${StickerShareTimeBean.SHARE_TIME_COLUMN} FROM $STICKER_SHARE_TIME_TABLE_NAME
        WHERE ${StickerShareTimeBean.STICKER_UUID_COLUMN} LIKE :uuid
        """
    )
    fun getShareTimeByUuid(uuid: String): List<Long>

    @Transaction
    @Query("DELETE FROM $STICKER_SHARE_TIME_TABLE_NAME")
    fun deleteAll(): Int

    @Transaction
    @Query(
        """
        DELETE FROM $STICKER_SHARE_TIME_TABLE_NAME
        WHERE ${StickerShareTimeBean.STICKER_UUID_COLUMN} LIKE :uuid
        """
    )
    fun deleteShareTimeByUuid(uuid: String): Int

    @Transaction
    @Query(
        """
        DELETE FROM $STICKER_SHARE_TIME_TABLE_NAME
        WHERE ${StickerShareTimeBean.SHARE_TIME_COLUMN} < :timestamp
        """
    )
    fun deleteShareTimeBefore(timestamp: Long): Int
}