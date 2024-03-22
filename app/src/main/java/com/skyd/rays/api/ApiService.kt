package com.skyd.rays.api

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.skyd.rays.api.strategy.SearchStickersStrategy
import com.skyd.rays.appContext
import com.skyd.rays.ext.dataStore
import com.skyd.rays.ext.getOrDefault
import com.skyd.rays.model.db.AppDatabase
import com.skyd.rays.model.preference.ApiGrantPreference
import com.skyd.rays.model.respository.SearchRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

@AndroidEntryPoint
class ApiService : Service() {
    @Inject
    lateinit var searchRepository: SearchRepository

    @Inject
    lateinit var json: Json

    inner class ApiServiceBinder : IApiService.Stub() {
        override fun searchStickers(requestPackage: String?, keyword: String?) = runBlocking {
            with(AppDatabase.getInstance(appContext)) {
                if (requestPackage.isNullOrBlank() || !appContext.dataStore.getOrDefault(
                        ApiGrantPreference
                    ) || !apiGrantPackageDao().packageEnable(requestPackage)
                ) {
                    return@runBlocking null
                }
            }

            json.encodeToString(
                SearchStickersStrategy.execute(searchRepository, keyword, requestPackage!!)
            )
        }
    }

    override fun onBind(intent: Intent): IBinder = ApiServiceBinder()
}