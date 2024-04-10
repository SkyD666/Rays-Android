package com.skyd.rays.api.strategy

import android.content.Intent
import androidx.core.content.FileProvider
import com.skyd.rays.api.ApiStickerWithTags
import com.skyd.rays.appContext
import com.skyd.rays.model.respository.SearchRepository
import com.skyd.rays.util.stickerUuidToFile
import kotlinx.coroutines.flow.first

object SearchStickersStrategy {
    suspend fun execute(
        repo: SearchRepository,
        keyword: String?,
        requestPackage: String,
    ): List<ApiStickerWithTags> {
        val result = repo
            .requestStickerWithTagsListFlow(keyword.orEmpty())
            .first()
            .map {
                val uri = FileProvider.getUriForFile(
                    appContext,
                    "${appContext.packageName}.fileprovider",
                    stickerUuidToFile(it.sticker.uuid)
                )
                appContext.grantUriPermission(
                    requestPackage,
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                ApiStickerWithTags.fromStickerWithTags(stickerWithTags = it, uri = uri.toString())
            }
        return result
    }
}