package com.skyd.rays.model.bean

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import com.skyd.rays.base.BaseBean
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

const val TAG_TABLE_NAME = "Tag"

@Parcelize
@Serializable
@Entity(
    tableName = TAG_TABLE_NAME,
    primaryKeys = [TagBean.STICKER_UUID_COLUMN, TagBean.TAG_COLUMN],
    foreignKeys = [
        ForeignKey(
            entity = StickerBean::class,
            parentColumns = [StickerBean.UUID_COLUMN],
            childColumns = [TagBean.STICKER_UUID_COLUMN],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TagBean(
    @ColumnInfo(name = STICKER_UUID_COLUMN)
    var stickerUuid: String,
    @ColumnInfo(name = TAG_COLUMN)
    var tag: String,
    @ColumnInfo(name = CREATE_TIME_COLUMN)
    var createTime: Long,
) : BaseBean, Parcelable {
    constructor(
        tag: String,
    ) : this(
        stickerUuid = "",
        tag = tag,
        createTime = System.currentTimeMillis(),
    )

    fun fields(): List<Any> {
        return listOf(stickerUuid, tag, createTime)
    }

    override fun toString(): String {
        return "$stickerUuid,$tag,$createTime"
    }

    companion object {
        const val STICKER_UUID_COLUMN = "stickerUuid"
        const val TAG_COLUMN = "tag"
        const val CREATE_TIME_COLUMN = "createTime"

        val columnName: List<Any> =
            listOf(STICKER_UUID_COLUMN, TAG_COLUMN, CREATE_TIME_COLUMN)
    }
}

