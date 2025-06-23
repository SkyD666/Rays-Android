package com.skyd.rays.ui.screen.settings.shareconfig

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.FileCopy
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.MoveDown
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
import com.skyd.rays.model.preference.share.CopyStickerToClipboardWhenSharingPreference
import com.skyd.rays.model.preference.share.StickerExtNamePreference
import com.skyd.rays.ui.local.LocalCopyStickerToClipboardWhenSharing
import com.skyd.rays.ui.local.LocalStickerExtName
import com.skyd.rays.ui.screen.settings.shareconfig.autoshare.AutoShareRoute
import com.skyd.rays.ui.screen.settings.shareconfig.uristringshare.UriStringShareRoute
import com.skyd.settings.BaseSettingsItem
import com.skyd.settings.SettingsLazyColumn
import com.skyd.settings.SwitchSettingsItem
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable


@Serializable
data object ShareConfigRoute

@Composable
fun ShareConfigScreen() {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val navController = LocalNavController.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            ComponeTopBar(
                style = ComponeTopBarStyle.LargeFlexible,
                scrollBehavior = scrollBehavior,
                title = { Text(text = stringResource(R.string.share_config_screen_name)) },
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
                        imageVector = Icons.Outlined.Description,
                        checked = LocalStickerExtName.current,
                        text = stringResource(R.string.share_config_screen_file_extension),
                        description = stringResource(R.string.share_config_screen_file_extension_description),
                        onCheckedChange = {
                            StickerExtNamePreference.put(
                                scope = scope,
                                context = context,
                                value = it
                            )
                        }
                    )
                }
                item {
                    SwitchSettingsItem(
                        imageVector = Icons.Outlined.FileCopy,
                        checked = LocalCopyStickerToClipboardWhenSharing.current,
                        text = stringResource(R.string.share_config_screen_copy_sticker_to_clipboard),
                        description = stringResource(R.string.share_config_screen_copy_sticker_to_clipboard_description),
                        onCheckedChange = {
                            scope.launch {
                                CopyStickerToClipboardWhenSharingPreference.put(
                                    scope = scope,
                                    context = context,
                                    value = it
                                )
                            }
                        }
                    )
                }
                item {
                    BaseSettingsItem(
                        icon = rememberVectorPainter(image = Icons.Outlined.Link),
                        text = stringResource(id = R.string.uri_string_share_screen_name),
                        descriptionText = stringResource(id = R.string.uri_string_share_screen_description),
                        onClick = { navController.navigate(UriStringShareRoute) }
                    )
                }
                item {
                    BaseSettingsItem(
                        icon = rememberVectorPainter(image = Icons.Outlined.MoveDown),
                        text = stringResource(id = R.string.auto_share_screen_name),
                        descriptionText = stringResource(id = R.string.auto_share_screen_description),
                        onClick = { navController.navigate(AutoShareRoute) }
                    )
                }
            }
        }
    }
}

