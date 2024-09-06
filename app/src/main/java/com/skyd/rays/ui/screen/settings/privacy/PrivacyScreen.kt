package com.skyd.rays.ui.screen.settings.privacy

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BlurOn
import androidx.compose.material.icons.outlined.Screenshot
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.skyd.rays.R
import com.skyd.rays.ext.activity
import com.skyd.rays.model.preference.privacy.DisableScreenshotPreference
import com.skyd.rays.ui.component.BaseSettingsItem
import com.skyd.rays.ui.component.RaysTopBar
import com.skyd.rays.ui.component.RaysTopBarStyle
import com.skyd.rays.ui.component.SwitchSettingsItem
import com.skyd.rays.ui.local.LocalDisableScreenshot
import com.skyd.rays.ui.local.LocalNavController
import com.skyd.rays.ui.screen.settings.privacy.blurstickers.BLUR_STICKERS_SCREEN_ROUTE


const val PRIVACY_SCREEN_ROUTE = "privacyScreen"

@Composable
fun PrivacyScreen() {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val context = LocalContext.current
    val navController = LocalNavController.current
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
                    imageVector = Icons.Outlined.Screenshot,
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
            item {
                BaseSettingsItem(
                    painter = rememberVectorPainter(Icons.Outlined.BlurOn),
                    text = stringResource(R.string.blur_stickers_screen_name),
                    descriptionText = stringResource(R.string.blur_stickers_screen_description),
                    onClick = { navController.navigate(BLUR_STICKERS_SCREEN_ROUTE) },
                )
            }
        }
    }
}
