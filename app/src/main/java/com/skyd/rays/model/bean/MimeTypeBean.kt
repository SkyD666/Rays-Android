package com.skyd.rays.model.bean

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.skyd.rays.base.BaseBean
import kotlinx.serialization.Serializable

const val MIME_TYPE_TABLE_NAME = "MimeType"

@Serializable
@Entity(
    tableName = MIME_TYPE_TABLE_NAME,
    foreignKeys = [
        ForeignKey(
            entity = StickerBean::class,
            parentColumns = [StickerBean.UUID_COLUMN],
            childColumns = [MimeTypeBean.STICKER_UUID_COLUMN],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class MimeTypeBean(
    @PrimaryKey
    @ColumnInfo(name = STICKER_UUID_COLUMN)
    var stickerUuid: String,
    @ColumnInfo(name = STICKER_MD5_COLUMN)
    var stickerMd5: String,
    @ColumnInfo(name = MIME_TYPE_COLUMN)
    var mimeType: String,
) : BaseBean {
    companion object {
        const val STICKER_UUID_COLUMN = "stickerUuid"
        const val STICKER_MD5_COLUMN = "stickerMd5"
        const val MIME_TYPE_COLUMN = "mimeType"
    }
}

