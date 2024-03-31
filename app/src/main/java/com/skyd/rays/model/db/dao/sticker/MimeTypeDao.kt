package com.skyd.rays.model.db.dao.sticker

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.MapColumn
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.skyd.rays.model.bean.MIME_TYPE_TABLE_NAME
import com.skyd.rays.model.bean.MimeTypeBean
import com.skyd.rays.model.bean.MimeTypeBean.Companion.MIME_TYPE_COLUMN
import com.skyd.rays.model.bean.MimeTypeBean.Companion.STICKER_MD5_COLUMN
import com.skyd.rays.model.bean.MimeTypeBean.Companion.STICKER_UUID_COLUMN
import com.skyd.rays.model.bean.STICKER_TABLE_NAME
import com.skyd.rays.model.bean.StickerBean

@Dao
interface MimeTypeDao {
    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun setMimeType(mimeTypeBean: MimeTypeBean)

    @Transaction
    @Query(
        """INSERT OR REPLACE INTO $MIME_TYPE_TABLE_NAME
            ($STICKER_UUID_COLUMN, $STICKER_MD5_COLUMN, $MIME_TYPE_COLUMN)
            VALUES (
                :stickerUuid,
                (SELECT $STICKER_MD5_COLUMN FROM $STICKER_TABLE_NAME
                    WHERE ${StickerBean.UUID_COLUMN} LIKE :stickerUuid),
                :mimeType)    
        """
    )
    fun setMimeType(stickerUuid: String, mimeType: String)

    @Transaction
    @Query(
        """SELECT $MIME_TYPE_COLUMN FROM $MIME_TYPE_TABLE_NAME
           WHERE $STICKER_UUID_COLUMN LIKE :stickerUuid AND
                 $STICKER_MD5_COLUMN LIKE (SELECT $STICKER_MD5_COLUMN FROM $STICKER_TABLE_NAME
                     WHERE ${StickerBean.UUID_COLUMN} LIKE :stickerUuid)
        """
    )
    fun getMimeTypeOrNull(stickerUuid: String): String?

    @Transaction
    @Query(
        """DELETE FROM $MIME_TYPE_TABLE_NAME
           WHERE $STICKER_UUID_COLUMN LIKE :stickerUuid
        """
    )
    fun delete(stickerUuid: String)

    @Transaction
    fun getMimeTypeOrNull(
        stickerUuid: String,
        deleteInvalid: Boolean = true,
    ): String? {
        val mimeType = getMimeTypeOrNull(stickerUuid)
        if (mimeType == null && deleteInvalid) {
            delete(stickerUuid)
        }
        return mimeType
    }

    @Transaction
    @Query(
        """
        SELECT $STICKER_UUID_COLUMN, $MIME_TYPE_COLUMN FROM $MIME_TYPE_TABLE_NAME
        WHERE $STICKER_UUID_COLUMN IN (:stickerUuids)
        """
    )
    fun getStickerMimeTypes(
        stickerUuids: List<String>,
    ): Map<@MapColumn(columnName = STICKER_UUID_COLUMN) String,
            @MapColumn(columnName = MIME_TYPE_COLUMN) String>
}