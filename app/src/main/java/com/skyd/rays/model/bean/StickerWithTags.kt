package com.skyd.rays.model.bean

import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.serialization.Serializable

@Serializable
data class StickerWithTags(
    @Embedded val sticker: StickerBean,
    @Relation(
        parentColumn = StickerBean.UUID_COLUMN,
        entityColumn = TagBean.STICKER_UUID_COLUMN
    )
    val tags: List<TagBean>
)
