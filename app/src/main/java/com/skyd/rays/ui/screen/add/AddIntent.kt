package com.skyd.rays.ui.screen.add

import android.net.Uri
import com.skyd.rays.base.IUiIntent
import com.skyd.rays.model.bean.StickerWithTags

sealed class AddIntent : IUiIntent {
    data class AddNewStickerWithTags(val stickerWithTags: StickerWithTags, val stickerUri: Uri) :
        AddIntent()

    data class GetStickerWithTags(val stickerUuid: String) : AddIntent()

    data class GetSuggestTags(val sticker: Uri) : AddIntent()
}