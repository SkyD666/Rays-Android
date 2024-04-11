package com.skyd.rays.model.bean

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import com.skyd.rays.base.BaseBean
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

const val STICKER_SHARE_TIME_TABLE_NAME = "StickerShareTime"

@Parcelize
@Serializable
@Entity(
    tableName = STICKER_SHARE_TIME_TABLE_NAME,
    primaryKeys = [
        StickerShareTimeBean.STICKER_UUID_COLUMN,
        StickerShareTimeBean.SHARE_TIME_COLUMN
    ],
    foreignKeys = [
        ForeignKey(
            entity = StickerBean::class,
            parentColumns = [StickerBean.UUID_COLUMN],
            childColumns = [StickerShareTimeBean.STICKER_UUID_COLUMN],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class StickerShareTimeBean(
    @ColumnInfo(name = STICKER_UUID_COLUMN)
    var stickerUuid: String,
    @ColumnInfo(name = SHARE_TIME_COLUMN)
    var shareTime: Long,
) : BaseBean, Parcelable {
    companion object {
        const val STICKER_UUID_COLUMN = "stickerUuid"
        const val SHARE_TIME_COLUMN = "shareTime"
    }
}

