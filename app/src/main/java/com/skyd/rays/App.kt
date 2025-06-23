package com.skyd.rays

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.skyd.rays.di.databaseModule
import com.skyd.rays.di.ioModule
import com.skyd.rays.di.pagingModule
import com.skyd.rays.di.repositoryModule
import com.skyd.rays.di.viewModelModule
import com.skyd.rays.ext.dataStore
import com.skyd.rays.ext.getOrDefault
import com.skyd.rays.model.preference.theme.DarkModePreference
import com.skyd.rays.util.CrashHandler
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        appContext = this

        startKoin {
            androidLogger()
            androidContext(this@App)
            modules(databaseModule, pagingModule, ioModule, repositoryModule, viewModelModule)
        }

        AppCompatDelegate.setDefaultNightMode(dataStore.getOrDefault(DarkModePreference))

        CrashHandler.init(this)
    }
}

lateinit var appContext: Context