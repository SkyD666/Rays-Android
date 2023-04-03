package com.skyd.rays

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.skyd.rays.ext.dataStore
import com.skyd.rays.ext.get
import com.skyd.rays.model.preference.theme.DarkModePreference
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        appContext = this

        AppCompatDelegate.setDefaultNightMode(
            dataStore.get(DarkModePreference.key) ?: DarkModePreference.default
        )

    }
}

lateinit var appContext: Context