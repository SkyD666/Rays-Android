package com.skyd.rays.model.bean

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import java.io.File

@Immutable
@Parcelize
@Serializable
data class StickerWithTags(
    @Embedded val sticker: StickerBean,
    @Relation(
        parentColumn = StickerBean.UUID_COLUMN,
        entityColumn = TagBean.STICKER_UUID_COLUMN
    )
    val tags: List<TagBean>
) : Parcelable

@Parcelize
data class StickerWithTagsAndFile(
    val stickerWithTags: StickerWithTags,
    val stickerFile: File
) : Parcelable