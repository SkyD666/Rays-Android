package com.skyd.rays.ui.screen.settings.data.importexport.cloud.webdav

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.Dns
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Recycling
import androidx.compose.material.icons.outlined.RestoreFromTrash
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skyd.compone.component.ComponeIconButton
import com.skyd.compone.component.ComponeTopBar
import com.skyd.compone.component.ComponeTopBarStyle
import com.skyd.compone.component.dialog.ComponeDialog
import com.skyd.compone.component.dialog.DeleteWarningDialog
import com.skyd.compone.component.dialog.TextFieldDialog
import com.skyd.compone.component.dialog.WaitingDialog
import com.skyd.rays.R
import com.skyd.rays.base.mvi.MviEventListener
import com.skyd.rays.base.mvi.getDispatcher
import com.skyd.rays.ext.secretSharedPreferences
import com.skyd.rays.ext.toDateTimeString
import com.skyd.rays.model.bean.BackupInfo
import com.skyd.rays.model.bean.WebDavWaitingInfo
import com.skyd.rays.model.preference.WebDavServerPreference
import com.skyd.rays.ui.component.RaysLottieAnimation
import com.skyd.rays.ui.local.LocalWebDavServer
import com.skyd.settings.BaseSettingsItem
import com.skyd.settings.CategorySettingsItem
import com.skyd.settings.SettingsLazyColumn
import com.skyd.settings.TipSettingsItem
import com.skyd.settings.dsl.SettingsLazyListScope
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel


@Serializable
data object WebDavRoute

private const val WEBDAV_ACCOUNT_KEY = "webDavAccount"
private const val WEBDAV_PASSWORD_KEY = "webDavPassword"

@Composable
fun WebDavScreen(viewModel: WebDavViewModel = koinViewModel()) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var openWarningDialog by rememberSaveable { mutableStateOf(false) }
    var waitingDialogData by rememberSaveable { mutableStateOf<WebDavWaitingInfo?>(null) }
    var openDeleteWarningDialog by rememberSaveable { mutableStateOf<String?>(null) }
    var openInputDialog by rememberSaveable { mutableStateOf(false) }
    var inputDialogInfo by remember {
        mutableStateOf<Triple<String, String, (String) -> Unit>>(Triple("", "") {})
    }
    var inputDialogIsPassword by rememberSaveable { mutableStateOf(false) }
    var openRecycleBinBottomSheet by rememberSaveable { mutableStateOf(false) }
    val uiState by viewModel.viewState.collectAsStateWithLifecycle()
    val dispatch = viewModel.getDispatcher(startWith = WebDavIntent.Init)

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            ComponeTopBar(
                style = ComponeTopBarStyle.LargeFlexible,
                scrollBehavior = scrollBehavior,
                title = { Text(text = stringResource(R.string.webdav_screen_name)) },
                actions = {
                    ComponeIconButton(
                        onClick = { openWarningDialog = true },
                        imageVector = Icons.Outlined.Info,
                        contentDescription = stringResource(R.string.info)
                    )
                },
            )
        }
    ) { paddingValues ->
        val server = LocalWebDavServer.current
        var account by rememberSaveable { mutableStateOf("") }
        var password by rememberSaveable { mutableStateOf("") }

        LaunchedEffect(Unit) {
            account = secretSharedPreferences().getString("webDavAccount", null).orEmpty()
            password = secretSharedPreferences().getString("webDavPassword", null).orEmpty()
        }

        val webDavIncompleteInfo = remember {
            {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = context.getString(R.string.webdav_screen_info_incomplete),
                        withDismissAction = true
                    )
                }
            }
        }

        SettingsLazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = paddingValues,
        ) {
            webDavItem(
                server = server,
                account = account,
                password = password,
                onServerItemClick = {
                    inputDialogInfo = Triple(
                        context.getString(R.string.webdav_screen_input_server), server
                    ) {
                        openInputDialog = false
                        WebDavServerPreference.put(
                            context = context,
                            scope = scope,
                            value = if (it.isBlank()) {
                                WebDavServerPreference.default
                            } else {
                                if (it.endsWith("/")) it else "$it/"
                            }
                        )
                    }
                    inputDialogIsPassword = false
                    openInputDialog = true
                },
                onAccountItemClick = {
                    inputDialogInfo = Triple(
                        context.getString(R.string.webdav_screen_input_account), account
                    ) {
                        openInputDialog = false
                        if (it.isBlank()) {
                            account = ""
                            secretSharedPreferences().edit { remove(WEBDAV_ACCOUNT_KEY) }
                        } else {
                            account = it
                            secretSharedPreferences().edit { putString(WEBDAV_ACCOUNT_KEY, it) }
                        }
                    }
                    inputDialogIsPassword = false
                    openInputDialog = true
                },
                onPasswordItemClick = {
                    inputDialogInfo = Triple(
                        context.getString(R.string.webdav_screen_input_password), password
                    ) {
                        openInputDialog = false
                        if (it.isBlank()) {
                            password = ""
                            secretSharedPreferences().edit { remove(WEBDAV_PASSWORD_KEY) }
                        } else {
                            password = it
                            secretSharedPreferences().edit { putString(WEBDAV_PASSWORD_KEY, it) }
                        }
                    }
                    inputDialogIsPassword = true
                    openInputDialog = true
                }
            )
            syncItem(
                onPullItemClick = {
                    if (checkWebDavInfo(server = server, account = account, password = password)) {
                        dispatch(
                            WebDavIntent.StartDownload(
                                data = WebDavAccountData(
                                    website = server,
                                    username = account,
                                    password = password
                                ),
                            )
                        )
                    } else {
                        webDavIncompleteInfo()
                    }
                },
                onPushItemClick = {
                    if (checkWebDavInfo(server = server, account = account, password = password)) {
                        dispatch(
                            WebDavIntent.StartUpload(
                                data = WebDavAccountData(
                                    website = server,
                                    username = account,
                                    password = password
                                ),
                            )
                        )
                    } else {
                        webDavIncompleteInfo()
                    }
                },
                onRemoteRecycleBinItemClick = {
                    if (checkWebDavInfo(server = server, account = account, password = password)) {
                        dispatch(
                            WebDavIntent.GetRemoteRecycleBin(
                                data = WebDavAccountData(
                                    website = server,
                                    username = account,
                                    password = password
                                ),
                            )
                        )
                        openRecycleBinBottomSheet = true
                    } else {
                        webDavIncompleteInfo()
                    }
                }
            )
        }

        WaitingDialog(
            visible = uiState.loadingDialog,
            currentValue = waitingDialogData?.current,
            totalValue = waitingDialogData?.total,
            msg = waitingDialogData?.msg.orEmpty() + "\n\n" + stringResource(id = R.string.data_sync_warning),
        )
        DeleteWarningDialog(
            visible = openDeleteWarningDialog != null,
            onDismissRequest = { openDeleteWarningDialog = null },
            onDismiss = { openDeleteWarningDialog = null },
            onConfirm = {
                if (openDeleteWarningDialog.isNullOrBlank()) {
                    dispatch(
                        WebDavIntent.ClearRemoteRecycleBin(
                            data = WebDavAccountData(
                                website = server,
                                username = account,
                                password = password
                            ),
                        )
                    )
                } else {
                    dispatch(
                        WebDavIntent.DeleteFromRemoteRecycleBin(
                            data = WebDavAccountData(
                                website = server,
                                username = account,
                                password = password
                            ),
                            uuid = openDeleteWarningDialog!!
                        )
                    )
                }
                openDeleteWarningDialog = null
            }
        )
        ComponeDialog(
            visible = openWarningDialog,
            onDismissRequest = { openWarningDialog = false },
            title = {
                Text(text = stringResource(id = R.string.dialog_warning))
            },
            text = {
                Text(text = stringResource(id = R.string.data_sync_warning))
            },
            confirmButton = {
                TextButton(onClick = { openWarningDialog = false }) {
                    Text(text = stringResource(id = R.string.dialog_ok))
                }
            }
        )
        TextFieldDialog(
            visible = openInputDialog,
            titleText = inputDialogInfo.first,
            value = inputDialogInfo.second,
            maxLines = 1,
            isPassword = inputDialogIsPassword,
            onDismissRequest = { openInputDialog = false },
            onConfirm = inputDialogInfo.third,
            enableConfirm = { true },
            onValueChange = {
                inputDialogInfo = inputDialogInfo.copy(second = it)
            },
        )
        if (openRecycleBinBottomSheet) {
            RecycleBinBottomSheet(
                uiState = uiState,
                onDismissRequest = { openRecycleBinBottomSheet = false },
                onRestore = {
                    dispatch(
                        WebDavIntent.RestoreFromRemoteRecycleBin(
                            data = WebDavAccountData(
                                website = server,
                                username = account,
                                password = password
                            ), uuid = it
                        )
                    )
                },
                onDelete = { openDeleteWarningDialog = it },
                onClear = { openDeleteWarningDialog = "" }
            )
        }
    }

    MviEventListener(viewModel.singleEvent) { event ->
        when (event) {
            is WebDavEvent.DownloadResultEvent.Error -> snackbarHostState.showSnackbar(
                context.getString(R.string.failed_info, event.msg),
            )

            is WebDavEvent.DownloadResultEvent.Success -> snackbarHostState.showSnackbar(
                context.resources.getQuantityString(
                    R.plurals.webdav_screen_download_success,
                    event.result.count,
                    event.result.time / 1000.0f, event.result.count
                ),
            )

            is WebDavEvent.GetRemoteRecycleBinResultEvent.Error -> snackbarHostState.showSnackbar(
                context.getString(R.string.failed_info, event.msg),
            )

            is WebDavEvent.UploadResultEvent.Error -> snackbarHostState.showSnackbar(
                context.getString(R.string.failed_info, event.msg),
            )

            is WebDavEvent.UploadResultEvent.Success -> snackbarHostState.showSnackbar(
                context.resources.getQuantityString(
                    R.plurals.webdav_screen_upload_success,
                    event.result.count,
                    event.result.time / 1000.0f, event.result.count
                ),
            )
        }
    }

    waitingDialogData = when (val uploadProgressState = uiState.uploadProgressState) {
        UploadProgressState.None -> {
            when (val downloadProgressState = uiState.downloadProgressState) {
                DownloadProgressState.None -> null
                is DownloadProgressState.Progress -> downloadProgressState.info
            }
        }

        is UploadProgressState.Progress -> uploadProgressState.info
    }
}

@Composable
private fun RecycleBinBottomSheet(
    uiState: WebDavState,
    onDismissRequest: () -> Unit,
    onRestore: (String) -> Unit,
    onDelete: (String) -> Unit,
    onClear: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismissRequest) {
        Row(
            Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                text = stringResource(R.string.webdav_screen_remote_recycle_bin),
                style = MaterialTheme.typography.titleLarge
            )
            Button(onClick = onClear) {
                Text(text = stringResource(R.string.webdav_screen_clear_remote_recycle_bin))
            }
        }
        LazyColumn(contentPadding = PaddingValues(vertical = 10.dp)) {
            val getRemoteRecycleBinResultUiState = uiState.getRemoteRecycleBinResultState
            if (getRemoteRecycleBinResultUiState is GetRemoteRecycleBinResultState.Success &&
                getRemoteRecycleBinResultUiState.result.isNotEmpty()
            ) {
                val list: List<BackupInfo> = getRemoteRecycleBinResultUiState.result
                items(list.size) {
                    ListItem(
                        headlineContent = { Text(text = list[it].uuid) },
                        supportingContent = {
                            Text(
                                text = stringResource(
                                    R.string.webdav_screen_last_modified_time,
                                    list[it].modifiedTime.toDateTimeString()
                                )
                            )
                        },
                        trailingContent = {
                            Row {
                                ComponeIconButton(
                                    imageVector = Icons.Outlined.RestoreFromTrash,
                                    contentDescription = stringResource(R.string.webdav_screen_restore),
                                    onClick = { onRestore(list[it].uuid) }
                                )
                                ComponeIconButton(
                                    imageVector = Icons.Outlined.DeleteForever,
                                    contentDescription = stringResource(R.string.webdav_screen_delete),
                                    onClick = { onDelete(list[it].uuid) }
                                )
                            }
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                }
            } else {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        RaysLottieAnimation(
                            modifier = Modifier.size(48.dp),
                            resId = R.raw.lottie_genshin_impact_paimon_1
                        )
                        Text(
                            modifier = Modifier.padding(start = 10.dp),
                            textAlign = TextAlign.Center,
                            text = stringResource(R.string.webdav_screen_remote_recycle_bin_is_empty)
                        )
                    }
                }
            }
        }
    }
}

private fun SettingsLazyListScope.webDavItem(
    server: String,
    account: String,
    password: String,
    onServerItemClick: () -> Unit,
    onAccountItemClick: () -> Unit,
    onPasswordItemClick: () -> Unit,
) {
    group(category = { CategorySettingsItem(text = stringResource(id = R.string.webdav_screen_service_category)) }) {
        item {
            BaseSettingsItem(
                icon = rememberVectorPainter(image = Icons.Outlined.Dns),
                text = stringResource(id = R.string.webdav_screen_server),
                descriptionText = server.ifBlank {
                    stringResource(id = R.string.webdav_screen_server_description)
                },
                onClick = onServerItemClick
            )
        }
        item {
            BaseSettingsItem(
                icon = rememberVectorPainter(image = Icons.Outlined.AccountCircle),
                text = stringResource(id = R.string.webdav_screen_account),
                descriptionText = account.ifBlank {
                    stringResource(id = R.string.webdav_screen_account_description)
                },
                onClick = onAccountItemClick
            )
        }
        item {
            BaseSettingsItem(
                icon = rememberVectorPainter(image = Icons.Outlined.Key),
                text = stringResource(id = R.string.webdav_screen_password),
                descriptionText = stringResource(
                    id = if (password.isBlank()) R.string.webdav_screen_password_description
                    else R.string.webdav_screen_password_entered
                ),
                onClick = onPasswordItemClick
            )
        }
    }
}

private fun SettingsLazyListScope.syncItem(
    onPullItemClick: () -> Unit,
    onPushItemClick: () -> Unit,
    onRemoteRecycleBinItemClick: () -> Unit,
) {
    group(category = { CategorySettingsItem(text = stringResource(id = R.string.webdav_screen_sync_category)) }) {
        item {
            BaseSettingsItem(
                icon = rememberVectorPainter(image = Icons.Outlined.CloudDownload),
                text = stringResource(id = R.string.webdav_screen_download),
                descriptionText = stringResource(id = R.string.webdav_screen_download_description),
                onClick = onPullItemClick
            )
        }
        item {
            BaseSettingsItem(
                icon = rememberVectorPainter(image = Icons.Outlined.CloudUpload),
                text = stringResource(id = R.string.webdav_screen_upload),
                descriptionText = stringResource(id = R.string.webdav_screen_upload_description),
                onClick = onPushItemClick
            )
        }
        item {
            BaseSettingsItem(
                icon = rememberVectorPainter(image = Icons.Outlined.Recycling),
                text = stringResource(id = R.string.webdav_screen_remote_recycle_bin),
                descriptionText = stringResource(id = R.string.webdav_screen_remote_recycle_bin_description),
                onClick = onRemoteRecycleBinItemClick
            )
        }
    }
    item {
        TipSettingsItem(
            text = stringResource(R.string.webdav_screen_download_tip) + "\n\n" +
                    stringResource(R.string.webdav_screen_upload_tip) + "\n\n" +
                    stringResource(R.string.webdav_screen_remote_recycle_bin_tip)
        )
    }
}

private fun checkWebDavInfo(
    server: String,
    account: String,
    password: String,
) = server.isNotBlank() && account.isNotBlank() && password.isNotBlank()