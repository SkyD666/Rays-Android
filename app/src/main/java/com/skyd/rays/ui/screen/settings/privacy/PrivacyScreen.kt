package com.skyd.rays.ui.screen.settings.privacy

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Screenshot
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.skyd.rays.R
import com.skyd.rays.ext.activity
import com.skyd.rays.model.preference.DisableScreenshotPreference
import com.skyd.rays.ui.component.RaysTopBar
import com.skyd.rays.ui.component.RaysTopBarStyle
import com.skyd.rays.ui.component.SwitchSettingsItem
import com.skyd.rays.ui.local.LocalDisableScreenshot


const val PRIVACY_SCREEN_ROUTE = "privacyScreen"

@Composable
fun PrivacyScreen() {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            RaysTopBar(
                style = RaysTopBarStyle.Large,
                scrollBehavior = scrollBehavior,
                title = { Text(text = stringResource(R.string.privacy_screen_name)) },
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = paddingValues,
        ) {
            item {
                SwitchSettingsItem(
                    icon = Icons.Default.Screenshot,
                    checked = LocalDisableScreenshot.current,
                    text = stringResource(R.string.privacy_screen_disable_screenshot),
                    description = stringResource(R.string.privacy_screen_disable_screenshot_description),
                    onCheckedChange = {
                        DisableScreenshotPreference.put(
                            context = context,
                            scope = scope,
                            value = it
                        )
                        context.activity.recreate()
                    }
                )
            }
        }
    }
}
