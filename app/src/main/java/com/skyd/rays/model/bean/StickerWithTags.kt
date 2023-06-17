package com.skyd.rays.model.bean

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

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
