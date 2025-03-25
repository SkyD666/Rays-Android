package com.skyd.rays.ui.screen.settings.shareconfig.autoshare

import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.MoveDown
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.skyd.rays.R
import com.skyd.rays.ext.safeLaunch
import com.skyd.rays.model.preference.AutoShareIgnoreStrategyPreference
import com.skyd.rays.ui.component.BannerItem
import com.skyd.rays.ui.component.BaseSettingsItem
import com.skyd.rays.ui.component.RaysTopBar
import com.skyd.rays.ui.component.RaysTopBarStyle
import com.skyd.rays.ui.component.SwitchSettingsItem
import com.skyd.rays.ui.component.TipSettingsItem
import com.skyd.rays.ui.component.dialog.TextFieldDialog
import com.skyd.rays.ui.local.LocalAutoShareIgnoreStrategy
import com.skyd.rays.ui.service.isAccessibilityServiceRunning
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable


@Serializable
data object AutoShareRoute

@Composable
fun AutoShareScreen() {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var openIgnoreStrategyDialog by rememberSaveable { mutableStateOf(false) }
    val ignoreStrategy = LocalAutoShareIgnoreStrategy.current
    var ignoreStrategyValue by rememberSaveable { mutableStateOf(ignoreStrategy) }
    var autoShareEnabled by rememberSaveable { mutableStateOf(isAccessibilityServiceRunning(context)) }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            RaysTopBar(
                style = RaysTopBarStyle.Large,
                scrollBehavior = scrollBehavior,
                title = { Text(text = stringResource(R.string.auto_share_screen_name)) },
            )
        }
    ) { paddingValues ->
        val accessibleLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            autoShareEnabled = isAccessibilityServiceRunning(context)
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = context.getString(
                        if (autoShareEnabled) R.string.auto_share_screen_accessibility_granted
                        else R.string.auto_share_screen_accessibility_not_granted
                    ),
                    withDismissAction = true
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = paddingValues,
        ) {
            item {
                BannerItem {
                    SwitchSettingsItem(
                        imageVector = Icons.Outlined.MoveDown,
                        text = stringResource(id = R.string.enable),
                        checked = autoShareEnabled,
                        onCheckedChange = {
                            accessibleLauncher.safeLaunch(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                        }
                    )
                }
            }
            item {
                BaseSettingsItem(
                    painter = rememberVectorPainter(image = Icons.Outlined.Block),
                    text = stringResource(id = R.string.auto_share_screen_ignore),
                    descriptionText = stringResource(id = R.string.auto_share_screen_ignore_description),
                    enabled = autoShareEnabled,
                    onClick = { openIgnoreStrategyDialog = true }
                )
            }
            item {
                TipSettingsItem(
                    text = stringResource(id = R.string.auto_share_screen_supported_app)
                )
            }
        }

        TextFieldDialog(
            visible = openIgnoreStrategyDialog,
            title = stringResource(id = R.string.auto_share_screen_ignore_input_dialog_title),
            value = ignoreStrategyValue,
            onDismissRequest = { openIgnoreStrategyDialog = false },
            onValueChange = { ignoreStrategyValue = it },
            onConfirm = { str ->
                runCatching { Regex(str) }
                    .onSuccess {
                        AutoShareIgnoreStrategyPreference.put(
                            context = context,
                            scope = scope,
                            value = str,
                        )
                    }
                    .onFailure {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = context.getString(R.string.auto_share_screen_ignore_regex_format_error),
                                withDismissAction = true
                            )
                        }
                    }
                openIgnoreStrategyDialog = false
            },
        )
    }
}
