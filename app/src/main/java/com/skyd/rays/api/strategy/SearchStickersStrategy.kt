package com.skyd.rays.api.strategy

import android.content.Intent
import android.os.Parcelable
import androidx.core.content.FileProvider
import com.skyd.rays.appContext
import com.skyd.rays.model.bean.BaseBean
import com.skyd.rays.model.bean.StickerBean
import com.skyd.rays.model.bean.StickerWithTags
import com.skyd.rays.model.bean.TagBean
import com.skyd.rays.model.respository.HomeRepository
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

class SearchStickersStrategy : IApiStrategy {
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

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface StrategyEntryPoint {
        fun homeRepository(): HomeRepository
        fun json(): Json
    }

    override val name = "searchStickers"
    override suspend fun execute(data: Intent): Intent {
        val hiltEntryPoint =
            EntryPointAccessors.fromApplication(appContext, StrategyEntryPoint::class.java)

        val requestPackage = data.getStringExtra("requestPackage")
        val result = hiltEntryPoint.homeRepository()
            .requestStickerWithTagsList(data.getStringExtra("keyword").orEmpty())
            .first().data.orEmpty()
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
        return Intent().apply {
            putExtra(
                "result",
                hiltEntryPoint.json().encodeToString(result)
            )
        }
    }
}