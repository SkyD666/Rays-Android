package com.skyd.rays.api

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.Parcelable
import com.skyd.rays.api.strategy.SearchStickersStrategy
import com.skyd.rays.appContext
import com.skyd.rays.ext.dataStore
import com.skyd.rays.ext.getOrDefault
import com.skyd.rays.model.db.AppDatabase
import com.skyd.rays.model.preference.ApiGrantPreference
import com.skyd.rays.model.respository.SearchRepository
import kotlinx.coroutines.runBlocking
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject

class ApiService : Service() {
    @Inject
    lateinit var searchRepository: SearchRepository

    @Inject
    lateinit var json: Json

    inner class ApiServiceBinder : IApiService.Stub() {
        override fun searchStickers(
            requestPackage: String?,
            keyword: String?,
            startIndex: Int,
            size: Int,
        ) = runBlocking {
            with(AppDatabase.getInstance(appContext)) {
                if (requestPackage.isNullOrBlank() || !appContext.dataStore.getOrDefault(
                        ApiGrantPreference
                    ) || !apiGrantPackageDao().packageEnable(requestPackage)
                ) {
                    return@runBlocking null
                }
            }

            var dataList: List<ApiStickerWithTags> = if (startIndex < 0 || size <= 0) emptyList()
            else SearchStickersStrategy.execute(searchRepository, keyword, requestPackage!!)
            val safeStartIndex =
                if (dataList.isEmpty()) 0 else startIndex.coerceIn(0, dataList.size - 1)
            val safeEndIndex =
                if (dataList.isEmpty()) 0 else (startIndex + size).coerceIn(1, dataList.size)
            if (dataList.isNotEmpty()) {
                dataList = dataList.subList(safeStartIndex, safeEndIndex)
            }

            // The Binder transaction buffer has a limited fixed size, currently 1MB
            var jsonString: String
            var dataListSize = dataList.size
            do {
                jsonString = json.encodeToString(
                    ResultWrapper(
                        data = if (dataList.isEmpty()) dataList
                        else dataList.subList(0, dataListSize),
                        startIndex = safeStartIndex,
                        size = dataListSize,
                    )
                )
                dataListSize /= 2
            } while (jsonString.encodeToByteArray().size > 1024 * 1024)
            jsonString
        }
    }

    override fun onBind(intent: Intent): IBinder = ApiServiceBinder()

    @Parcelize
    @Serializable
    data class ResultWrapper(
        val startIndex: Int,
        val size: Int,
        val data: List<ApiStickerWithTags>,
    ) : Parcelable
}