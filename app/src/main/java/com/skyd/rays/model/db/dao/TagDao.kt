package com.skyd.rays.model.db.dao

import androidx.room.*
import com.skyd.rays.model.bean.TAG_TABLE_NAME
import com.skyd.rays.model.bean.TagBean
import com.skyd.rays.model.bean.TagBean.Companion.CREATE_TIME_COLUMN
import com.skyd.rays.model.bean.TagBean.Companion.STICKER_UUID_COLUMN
import com.skyd.rays.model.bean.TagBean.Companion.TAG_COLUMN
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {
    @Transaction
    @Query("SELECT * FROM $TAG_TABLE_NAME")
    fun getTagList(): List<TagBean>

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addTags(tags: List<TagBean>)

    @Transaction
    @Query(value = "DELETE FROM $TAG_TABLE_NAME WHERE $STICKER_UUID_COLUMN LIKE :stickerUuid")
    fun deleteTags(stickerUuid: String): Int

    @Transaction
    @Query("DELETE FROM $TAG_TABLE_NAME")
    fun deleteAllTags()

    @Transaction
    @Query(
        """SELECT 
            $STICKER_UUID_COLUMN, 
            MIN($TAG_COLUMN) AS $TAG_COLUMN, 
            MIN($CREATE_TIME_COLUMN) AS $CREATE_TIME_COLUMN 
        FROM $TAG_TABLE_NAME 
        WHERE LENGTH($TAG_COLUMN) < 10
        GROUP BY $STICKER_UUID_COLUMN
        LIMIT :count
        """
    )
    fun getRecommendTagsList(count: Int): Flow<List<TagBean>>

    @Transaction
    @Query(
        """SELECT 
            MIN($STICKER_UUID_COLUMN) AS $STICKER_UUID_COLUMN, 
            $TAG_COLUMN, 
            MIN($CREATE_TIME_COLUMN) AS $CREATE_TIME_COLUMN 
        FROM (SELECT * FROM $TAG_TABLE_NAME 
              GROUP BY $TAG_COLUMN
              HAVING COUNT(*) > 2)
        WHERE LENGTH($TAG_COLUMN) < 25
        GROUP BY $TAG_COLUMN
        ORDER BY RANDOM()
        LIMIT :count
        """
    )
    fun getRandomTagsList(count: Int): Flow<List<TagBean>>
}