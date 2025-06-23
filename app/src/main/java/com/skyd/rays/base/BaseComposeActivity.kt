package com.skyd.rays.base

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.skyd.rays.model.preference.SettingsProvider
import com.skyd.rays.ui.local.LocalDarkMode
import com.skyd.rays.ui.local.LocalWindowSizeClass
import com.skyd.rays.ui.theme.RaysTheme

open class BaseComposeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // A known bug: https://issuetracker.google.com/issues/387281251
        enableEdgeToEdge()
    }

    fun setContentBase(content: @Composable () -> Unit) = setContent {
        CompositionLocalProvider(
            LocalWindowSizeClass provides calculateWindowSizeClass(this@BaseComposeActivity)
        ) {
            SettingsProvider { RaysTheme(darkTheme = LocalDarkMode.current, content) }
        }
    }
}