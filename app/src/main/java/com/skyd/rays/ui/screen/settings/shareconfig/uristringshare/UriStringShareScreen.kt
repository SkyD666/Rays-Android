package com.skyd.rays.ui.screen.settings.shareconfig.uristringshare

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.DismissDirection
import androidx.compose.material3.DismissValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.skyd.rays.R
import com.skyd.rays.ext.showSnackbarWithLaunchedEffect
import com.skyd.rays.model.bean.UriStringSharePackageBean
import com.skyd.rays.model.preference.share.UriStringSharePreference
import com.skyd.rays.ui.component.BannerItem
import com.skyd.rays.ui.component.LocalUseColorfulIcon
import com.skyd.rays.ui.component.RaysIconButton
import com.skyd.rays.ui.component.RaysSwipeToDismiss
import com.skyd.rays.ui.component.RaysTopBar
import com.skyd.rays.ui.component.RaysTopBarStyle
import com.skyd.rays.ui.component.SwitchSettingsItem
import com.skyd.rays.ui.component.dialog.DeleteWarningDialog
import com.skyd.rays.ui.component.dialog.TextFieldDialog
import com.skyd.rays.ui.local.LocalUriStringShare


const val URI_STRING_SHARE_SCREEN_ROUTE = "uriStringShareScreen"

@Composable
fun UriStringShareScreen(viewModel: UriStringShareViewModel = hiltViewModel()) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var openAddDialog by rememberSaveable { mutableStateOf(false) }
    var openDeleteDialog by rememberSaveable { mutableStateOf<String?>(null) }
    var inputPackageName by rememberSaveable { mutableStateOf("") }
    val uiState by viewModel.uiStateFlow.collectAsStateWithLifecycle()
    val uiEvent by viewModel.uiEventFlow.collectAsStateWithLifecycle(initialValue = null)

    LaunchedEffect(Unit) {
        viewModel.sendUiIntent(UriStringShareIntent.GetAllUriStringShare)
    }

    uiEvent?.apply {
        when (addPackageNameUiEvent) {
            is AddPackageNameUiEvent.Failed -> {
                snackbarHostState.showSnackbarWithLaunchedEffect(
                    message = context.getString(R.string.failed_info, addPackageNameUiEvent.msg),
                    key2 = addPackageNameUiEvent,
                )
            }

            AddPackageNameUiEvent.Success,
            null -> Unit
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            RaysTopBar(
                style = RaysTopBarStyle.Large,
                scrollBehavior = scrollBehavior,
                title = { Text(text = stringResource(R.string.uri_string_share_screen_name)) },
                actions = {
                    RaysIconButton(
                        onClick = { openAddDialog = true },
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.uri_string_share_screen_add_app_package)
                    )
                },
            )
        }
    ) { paddingValues ->
        val uriStringShare = LocalUriStringShare.current
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = paddingValues,
        ) {
            item {
                BannerItem {
                    SwitchSettingsItem(
                        icon = Icons.Default.Link,
                        text = stringResource(id = R.string.enable),
                        checked = uriStringShare,
                        onCheckedChange = {
                            UriStringSharePreference.put(
                                context = context,
                                scope = scope,
                                value = it
                            )
                        }
                    )
                }
            }
            val uriStringShareResultUiState = uiState.uriStringShareResultUiState
            if (uriStringShareResultUiState is UriStringShareResultUiState.Success) {
                itemsIndexed(uriStringShareResultUiState.data) { _, item ->
                    CompositionLocalProvider(LocalUseColorfulIcon provides true) {
                        RaysSwipeToDismiss(
                            state = rememberDismissState(
                                confirmValueChange = { dismissValue ->
                                    if (dismissValue == DismissValue.DismissedToStart) {
                                        openDeleteDialog =
                                            item.uriStringSharePackageBean.packageName
                                    }
                                    false
                                }
                            ),
                            directions = setOf(DismissDirection.EndToStart),
                        ) {
                            SwitchSettingsItem(
                                modifier = Modifier.background(MaterialTheme.colorScheme.background),
                                icon = rememberDrawablePainter(drawable = item.appIcon),
                                checked = item.uriStringSharePackageBean.enabled,
                                enabled = uriStringShare,
                                text = item.appName,
                                description = item.uriStringSharePackageBean.packageName,
                                onCheckedChange = {
                                    viewModel.sendUiIntent(
                                        UriStringShareIntent.UpdateUriStringShare(
                                            item.uriStringSharePackageBean.copy(enabled = it)
                                        )
                                    )
                                },
                            )
                        }
                    }
                }
            }
        }

        TextFieldDialog(
            visible = openAddDialog,
            title = stringResource(id = R.string.uri_string_share_screen_add_app_package),
            maxLines = 1,
            placeholder = stringResource(id = R.string.uri_string_share_screen_add_app_package_example),
            onDismissRequest = { openAddDialog = false },
            value = inputPackageName,
            onValueChange = { inputPackageName = it },
            onConfirm = {
                viewModel.sendUiIntent(
                    UriStringShareIntent.UpdateUriStringShare(
                        UriStringSharePackageBean(packageName = it, enabled = true)
                    )
                )
                inputPackageName = ""
                openAddDialog = false
            }
        )

        DeleteWarningDialog(
            visible = openDeleteDialog != null,
            onDismissRequest = { openDeleteDialog = null },
            onDismiss = { openDeleteDialog = null },
            onConfirm = {
                viewModel.sendUiIntent(
                    UriStringShareIntent.DeleteUriStringShare(openDeleteDialog!!)
                )
                openDeleteDialog = null
            }
        )
    }
}
