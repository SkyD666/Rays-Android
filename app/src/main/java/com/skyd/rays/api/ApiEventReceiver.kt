package com.skyd.rays.api

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.skyd.rays.api.strategy.SearchStickersStrategy
import com.skyd.rays.ext.dataStore
import com.skyd.rays.ext.get
import com.skyd.rays.model.db.AppDatabase
import com.skyd.rays.model.preference.ApiGrantPreference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ApiEventReceiver : BroadcastReceiver() {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val apiStrategies = arrayOf(SearchStickersStrategy())

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        val requestPackage = intent.getStringExtra("requestPackage") ?: return
        val api = intent.getStringExtra("api") ?: return

        scope.launch {
            with(AppDatabase.getInstance(context)) {
                if (context.dataStore.get(ApiGrantPreference.key) == false ||
                    !apiGrantPackageDao().packageEnable(requestPackage)
                ) {
                    return@launch
                }
            }

            val newIntent = apiStrategies
                .firstOrNull { it.name == api }
                ?.execute(intent) ?: return@launch
            withContext(Dispatchers.Main) {
                newIntent.action = "${context.packageName}.api.result"
                newIntent.`package` = requestPackage
                context.sendBroadcast(newIntent)
            }
        }
    }
}