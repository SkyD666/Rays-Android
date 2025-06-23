package com.skyd.rays.ui.screen.settings.privacy

import androidx.compose.foundation.layout.fillMaxSize
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
import com.skyd.compone.component.ComponeTopBar
import com.skyd.compone.component.ComponeTopBarStyle
import com.skyd.compone.local.LocalNavController
import com.skyd.rays.R
import com.skyd.rays.ext.activity
import com.skyd.rays.model.preference.privacy.DisableScreenshotPreference
import com.skyd.rays.ui.local.LocalDisableScreenshot
import com.skyd.rays.ui.screen.settings.privacy.blurstickers.BlurStickersRoute
import com.skyd.settings.BaseSettingsItem
import com.skyd.settings.SettingsLazyColumn
import com.skyd.settings.SwitchSettingsItem
import kotlinx.serialization.Serializable


@Serializable
data object PrivacyRoute

@Composable
fun PrivacyScreen() {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val context = LocalContext.current
    val navController = LocalNavController.current
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            ComponeTopBar(
                style = ComponeTopBarStyle.LargeFlexible,
                scrollBehavior = scrollBehavior,
                title = { Text(text = stringResource(R.string.privacy_screen_name)) },
            )
        }
    ) { paddingValues ->
        SettingsLazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = paddingValues,
        ) {
            group {
                item {
                    SwitchSettingsItem(
                        imageVector = Icons.Outlined.Screenshot,
                        checked = LocalDisableScreenshot.current,
                        text = stringResource(R.string.privacy_screen_disable_screenshot),
                        description = stringResource(R.string.privacy_screen_disable_screenshot_description),
                        onCheckedChange = {
                            DisableScreenshotPreference.put(context, scope, it)
                            context.activity.recreate()
                        }
                    )
                }
                item {
                    BaseSettingsItem(
                        icon = rememberVectorPainter(Icons.Outlined.BlurOn),
                        text = stringResource(R.string.blur_stickers_screen_name),
                        descriptionText = stringResource(R.string.blur_stickers_screen_description),
                        onClick = { navController.navigate(BlurStickersRoute) },
                    )
                }
            }
        }
    }
}
