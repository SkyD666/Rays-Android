package com.skyd.rays.ui.screen.add

import android.net.Uri
import com.skyd.rays.base.mvi.MviIntent
import com.skyd.rays.model.bean.StickerWithTags
import com.skyd.rays.model.bean.UriWithStickerUuidBean

sealed interface AddIntent : MviIntent {
    data class Init(val initStickers: List<UriWithStickerUuidBean>) : AddIntent
    data class ProcessNext(val nextSticker: UriWithStickerUuidBean?) : AddIntent
    data class AddNewStickerWithTags(val stickerWithTags: StickerWithTags, val stickerUri: Uri) :
        AddIntent

    data class GetStickerWithTags(val stickerUuid: String) : AddIntent
    data class GetSuggestTags(val sticker: Uri) : AddIntent
    data class RemoveSuggestTag(val text: String) : AddIntent
    data class ReplaceWaitingListSingleSticker(
        val sticker: UriWithStickerUuidBean,
        val index: Int,
    ) : AddIntent

    data class RemoveWaitingListSingleSticker(val index: Int) : AddIntent

    data class AddTag(val text: String) : AddIntent
    data class RemoveTag(val text: String) : AddIntent
    data class AddToWaitingList(val stickers: List<UriWithStickerUuidBean>) : AddIntent
    data class AddAddToAllTag(val text: String) : AddIntent
    data class RemoveAddToAllTag(val text: String) : AddIntent
}