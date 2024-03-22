package com.skyd.rays.api

import android.os.Parcelable
import com.skyd.rays.base.BaseBean
import com.skyd.rays.model.bean.StickerBean
import com.skyd.rays.model.bean.StickerWithTags
import com.skyd.rays.model.bean.TagBean
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class ApiStickerWithTags(
    val uri: String,
    val sticker: StickerBean,
    val tags: List<TagBean>
) : BaseBean, Parcelable {
    companion object {
        fun fromStickerWithTags(
            stickerWithTags: StickerWithTags,
            uri: String
        ): ApiStickerWithTags {
            return ApiStickerWithTags(
                uri = uri,
                sticker = stickerWithTags.sticker,
                tags = stickerWithTags.tags,
            )
        }
    }
}