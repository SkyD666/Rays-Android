package com.skyd.rays.model.bean

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.skyd.rays.base.BaseBean
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import java.util.UUID

const val STICKER_TABLE_NAME = "Sticker"

@Parcelize
@Serializable
@Entity(tableName = STICKER_TABLE_NAME)
data class StickerBean(
    @PrimaryKey
    @ColumnInfo(name = UUID_COLUMN)
    var uuid: String,
    @ColumnInfo(name = TITLE_COLUMN)
    var title: String,
    @ColumnInfo(name = STICKER_MD5_COLUMN)
    var stickerMd5: String,
    @ColumnInfo(name = CLICK_COUNT_COLUMN)
    var clickCount: Long,
    @ColumnInfo(name = SHARE_COUNT_COLUMN)
    var shareCount: Long,
    @ColumnInfo(name = CREATE_TIME_COLUMN)
    var createTime: Long,
    @ColumnInfo(name = MODIFY_TIME_COLUMN)
    var modifyTime: Long?,
) : BaseBean, Parcelable {
    constructor(
        title: String,
        createTime: Long = System.currentTimeMillis(),
        uuid: String = UUID.randomUUID().toString(),
    ) : this(
        uuid = uuid,
        title = title,
        stickerMd5 = "",
        clickCount = 0L,
        shareCount = 0L,
        createTime = createTime,
        modifyTime = null,
    )

    fun fields(): List<Any?> {
        return listOf(uuid, title, stickerMd5, clickCount, shareCount, createTime, modifyTime)
    }

    override fun toString(): String {
        return "$uuid,$title,$stickerMd5,$clickCount,$shareCount,$createTime,$modifyTime"
    }

    companion object {
        const val UUID_COLUMN = "uuid"
        const val TITLE_COLUMN = "title"
        const val STICKER_MD5_COLUMN = "stickerMd5"
        const val CLICK_COUNT_COLUMN = "clickCount"
        const val SHARE_COUNT_COLUMN = "shareCount"
        const val CREATE_TIME_COLUMN = "createTime"
        const val MODIFY_TIME_COLUMN = "modifyTime"

        val columnName: List<Any> = listOf(
            UUID_COLUMN,
            TITLE_COLUMN,
            STICKER_MD5_COLUMN,
            CLICK_COUNT_COLUMN,
            SHARE_COUNT_COLUMN,
            CREATE_TIME_COLUMN,
            MODIFY_TIME_COLUMN,
        )
    }
}
