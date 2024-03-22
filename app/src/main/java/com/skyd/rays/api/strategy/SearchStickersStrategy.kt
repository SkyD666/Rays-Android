package com.skyd.rays.api.strategy

import android.content.Intent
import android.os.Parcelable
import androidx.core.content.FileProvider
import com.skyd.rays.api.ApiStickerWithTags
import com.skyd.rays.appContext
import com.skyd.rays.base.BaseBean
import com.skyd.rays.model.bean.StickerBean
import com.skyd.rays.model.bean.StickerWithTags
import com.skyd.rays.model.bean.TagBean
import com.skyd.rays.model.respository.SearchRepository
import com.skyd.rays.util.stickerUuidToFile
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object SearchStickersStrategy {
    suspend fun execute(
        repo: SearchRepository,
        keyword: String?,
        requestPackage: String,
    ): List<ApiStickerWithTags> {
        val result = repo
            .requestStickerWithTagsList(keyword.orEmpty())
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