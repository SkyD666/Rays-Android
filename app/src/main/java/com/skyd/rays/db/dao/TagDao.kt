package com.skyd.rays.db.dao

import androidx.room.*
import com.skyd.rays.model.bean.TAG_TABLE_NAME
import com.skyd.rays.model.bean.TagBean
import com.skyd.rays.model.bean.TagBean.Companion.STICKER_UUID_COLUMN

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
}