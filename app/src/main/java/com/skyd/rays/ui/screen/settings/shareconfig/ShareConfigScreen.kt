package com.skyd.rays.ui.screen.settings.shareconfig

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileCopy
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MoveDown
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
import com.skyd.rays.model.preference.share.CopyStickerToClipboardWhenSharingPreference
import com.skyd.rays.model.preference.share.StickerExtNamePreference
import com.skyd.rays.ui.component.BaseSettingsItem
import com.skyd.rays.ui.component.RaysTopBar
import com.skyd.rays.ui.component.RaysTopBarStyle
import com.skyd.rays.ui.component.SwitchSettingsItem
import com.skyd.rays.ui.local.LocalCopyStickerToClipboardWhenSharing
import com.skyd.rays.ui.local.LocalNavController
import com.skyd.rays.ui.local.LocalStickerExtName
import com.skyd.rays.ui.screen.settings.shareconfig.autoshare.AUTO_SHARE_SCREEN_ROUTE
import com.skyd.rays.ui.screen.settings.shareconfig.uristringshare.URI_STRING_SHARE_SCREEN_ROUTE
import kotlinx.coroutines.launch


const val SHARE_CONFIG_SCREEN_ROUTE = "shareConfigScreen"

@Composable
fun ShareConfigScreen() {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val navController = LocalNavController.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            RaysTopBar(
                style = RaysTopBarStyle.Large,
                scrollBehavior = scrollBehavior,
                title = { Text(text = stringResource(R.string.share_config_screen_name)) },
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
                    icon = Icons.Default.Description,
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
                    icon = Icons.Default.FileCopy,
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
                    icon = rememberVectorPainter(image = Icons.Default.Link),
                    text = stringResource(id = R.string.uri_string_share_screen_name),
                    descriptionText = stringResource(id = R.string.uri_string_share_screen_description),
                    onClick = { navController.navigate(URI_STRING_SHARE_SCREEN_ROUTE) }
                )
            }
            item {
                BaseSettingsItem(
                    icon = rememberVectorPainter(image = Icons.Default.MoveDown),
                    text = stringResource(id = R.string.auto_share_screen_name),
                    descriptionText = stringResource(id = R.string.auto_share_screen_description),
                    onClick = { navController.navigate(AUTO_SHARE_SCREEN_ROUTE) }
                )
            }
        }
    }
}
