package com.skyd.rays.ui.screen.settings.shareconfig.uristringshare

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.skyd.compone.component.ComponeIconButton
import com.skyd.compone.component.ComponeTopBar
import com.skyd.compone.component.ComponeTopBarStyle
import com.skyd.compone.component.dialog.DeleteWarningDialog
import com.skyd.compone.component.dialog.TextFieldDialog
import com.skyd.rays.R
import com.skyd.rays.base.mvi.MviEventListener
import com.skyd.rays.base.mvi.getDispatcher
import com.skyd.rays.model.bean.UriStringSharePackageBean
import com.skyd.rays.model.preference.share.UriStringSharePreference
import com.skyd.rays.ui.component.RaysSwipeToDismiss
import com.skyd.rays.ui.local.LocalUriStringShare
import com.skyd.settings.BannerItem
import com.skyd.settings.LocalSettingsStyle
import com.skyd.settings.SettingsLazyColumn
import com.skyd.settings.SettingsStyle
import com.skyd.settings.SwitchSettingsItem
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel


@Serializable
data object UriStringShareRoute

@Composable
fun UriStringShareScreen(viewModel: UriStringShareViewModel = koinViewModel()) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var openAddDialog by rememberSaveable { mutableStateOf(false) }
    var openDeleteDialog by rememberSaveable { mutableStateOf<String?>(null) }
    var inputPackageName by rememberSaveable { mutableStateOf("") }
    val uiState by viewModel.viewState.collectAsStateWithLifecycle()
    val dispatch = viewModel.getDispatcher(startWith = UriStringShareIntent.GetAllUriStringShare)

    MviEventListener(viewModel.singleEvent) { event ->
        when (event) {
            is UriStringShareEvent.AddPackageNameUiEvent.Failed -> snackbarHostState.showSnackbar(
                context.getString(R.string.failed_info, event.msg),
            )

            UriStringShareEvent.AddPackageNameUiEvent.Success -> Unit
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            ComponeTopBar(
                style = ComponeTopBarStyle.LargeFlexible,
                scrollBehavior = scrollBehavior,
                title = { Text(text = stringResource(R.string.uri_string_share_screen_name)) },
                actions = {
                    ComponeIconButton(
                        onClick = { openAddDialog = true },
                        imageVector = Icons.Outlined.Add,
                        contentDescription = stringResource(R.string.uri_string_share_screen_add_app_package)
                    )
                },
            )
        }
    ) { paddingValues ->
        val uriStringShare = LocalUriStringShare.current
        SettingsLazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = paddingValues,
        ) {
            item {
                BannerItem {
                    SwitchSettingsItem(
                        imageVector = Icons.Outlined.Link,
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
            val uriStringShareResultUiState = uiState.uriStringShareResultState
            if (uriStringShareResultUiState is UriStringShareResultState.Success) {
                group {
                    items(count = uriStringShareResultUiState.data.size, isBaseItem = { true }) {
                        val item = uriStringShareResultUiState.data[it]
                        CompositionLocalProvider(
                            LocalSettingsStyle provides SettingsStyle(baseItemUseColorfulIcon = true)
                        ) {
                            RaysSwipeToDismiss(
                                state = rememberSwipeToDismissBoxState(
                                    confirmValueChange = { dismissValue ->
                                        if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                                            openDeleteDialog =
                                                item.uriStringSharePackageBean.packageName
                                        }
                                        false
                                    }
                                ),
                                enableDismissFromStartToEnd = false,
                                enableDismissFromEndToStart = true,
                            ) {
                                SwitchSettingsItem(
                                    modifier = Modifier.background(MaterialTheme.colorScheme.background),
                                    painter = rememberDrawablePainter(drawable = item.appIcon),
                                    checked = item.uriStringSharePackageBean.enabled,
                                    enabled = uriStringShare,
                                    text = item.appName,
                                    description = item.uriStringSharePackageBean.packageName,
                                    onCheckedChange = {
                                        dispatch(
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
        }

        TextFieldDialog(
            visible = openAddDialog,
            titleText = stringResource(id = R.string.uri_string_share_screen_add_app_package),
            maxLines = 1,
            placeholder = stringResource(id = R.string.uri_string_share_screen_add_app_package_example),
            onDismissRequest = { openAddDialog = false },
            value = inputPackageName,
            onValueChange = { inputPackageName = it },
            onConfirm = {
                dispatch(
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
                dispatch(
                    UriStringShareIntent.DeleteUriStringShare(openDeleteDialog!!)
                )
                openDeleteDialog = null
            }
        )
    }
}
