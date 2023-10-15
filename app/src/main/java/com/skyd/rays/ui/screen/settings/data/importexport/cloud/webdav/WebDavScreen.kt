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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material.icons.filled.Warning
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skyd.rays.R
import com.skyd.rays.appContext
import com.skyd.rays.base.LoadUiIntent
import com.skyd.rays.ext.dateTime
import com.skyd.rays.ext.editor
import com.skyd.rays.ext.secretSharedPreferences
import com.skyd.rays.ext.showSnackbarWithLaunchedEffect
import com.skyd.rays.model.bean.BackupInfo
import com.skyd.rays.model.bean.WebDavResultInfo
import com.skyd.rays.model.bean.WebDavWaitingInfo
import com.skyd.rays.model.preference.WebDavServerPreference
import com.skyd.rays.ui.component.BaseSettingsItem
import com.skyd.rays.ui.component.CategorySettingsItem
import com.skyd.rays.ui.component.RaysIconButton
import com.skyd.rays.ui.component.RaysLottieAnimation
import com.skyd.rays.ui.component.RaysTopBar
import com.skyd.rays.ui.component.RaysTopBarStyle
import com.skyd.rays.ui.component.dialog.DeleteWarningDialog
import com.skyd.rays.ui.component.dialog.RaysDialog
import com.skyd.rays.ui.component.dialog.TextFieldDialog
import com.skyd.rays.ui.component.dialog.WaitingDialog
import com.skyd.rays.ui.local.LocalWebDavServer
import kotlinx.coroutines.launch

const val WEBDAV_SCREEN_ROUTE = "webDavScreen"

@Composable
fun WebDavScreen(viewModel: WebDavViewModel = hiltViewModel()) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var openWarningDialog by rememberSaveable { mutableStateOf(false) }
    var openWaitingDialog by rememberSaveable { mutableStateOf(false) }
    var waitingDialogData by rememberSaveable { mutableStateOf<WebDavWaitingInfo?>(null) }
    var openDeleteWarningDialog by rememberSaveable { mutableStateOf<String?>(null) }
    var openInputDialog by rememberSaveable { mutableStateOf(false) }
    var inputDialogInfo by remember {
        mutableStateOf<Triple<String, String, (String) -> Unit>>(Triple("", "") {})
    }
    var inputDialogIsPassword by rememberSaveable { mutableStateOf(false) }
    var openRecycleBinBottomSheet by rememberSaveable { mutableStateOf(false) }
    val loadUiIntent by viewModel.loadUiIntentFlow.collectAsStateWithLifecycle(initialValue = null)
    val uiEvent by viewModel.uiEventFlow.collectAsStateWithLifecycle(initialValue = null)

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            RaysTopBar(
                style = RaysTopBarStyle.Large,
                scrollBehavior = scrollBehavior,
                title = { Text(text = stringResource(R.string.webdav_screen_name)) },
                actions = {
                    RaysIconButton(
                        onClick = { openWarningDialog = true },
                        imageVector = Icons.Default.Warning
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

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = paddingValues
        ) {
            item {
                CategorySettingsItem(
                    text = stringResource(id = R.string.webdav_screen_service_category)
                )
            }
            item {
                BaseSettingsItem(
                    icon = rememberVectorPainter(image = Icons.Default.Dns),
                    text = stringResource(id = R.string.webdav_screen_server),
                    descriptionText = server.ifBlank {
                        stringResource(id = R.string.webdav_screen_server_description)
                    },
                    onClick = {
                        inputDialogInfo = Triple(
                            appContext.getString(R.string.webdav_screen_input_server), server
                        ) {
                            openInputDialog = false
                            WebDavServerPreference.put(
                                context = context,
                                scope = scope,
                                value = if (!it.endsWith("/")) "$it/" else it
                            )
                        }
                        inputDialogIsPassword = false
                        openInputDialog = true
                    }
                )
                BaseSettingsItem(
                    icon = rememberVectorPainter(image = Icons.Default.AccountCircle),
                    text = stringResource(id = R.string.webdav_screen_account),
                    descriptionText = account.ifBlank {
                        stringResource(id = R.string.webdav_screen_account_description)
                    },
                    onClick = {
                        inputDialogInfo = Triple(
                            appContext.getString(R.string.webdav_screen_input_account), account
                        ) {
                            account = it
                            openInputDialog = false
                            secretSharedPreferences().editor { putString("webDavAccount", it) }
                        }
                        inputDialogIsPassword = false
                        openInputDialog = true
                    }
                )
                BaseSettingsItem(
                    icon = rememberVectorPainter(image = Icons.Default.Key),
                    text = stringResource(id = R.string.webdav_screen_password),
                    descriptionText = stringResource(
                        id = if (password.isBlank()) R.string.webdav_screen_password_description
                        else R.string.webdav_screen_password_entered
                    ),
                    onClick = {
                        inputDialogInfo = Triple(
                            appContext.getString(R.string.webdav_screen_input_password), password
                        ) {
                            password = it
                            openInputDialog = false
                            secretSharedPreferences().editor { putString("webDavPassword", it) }
                        }
                        inputDialogIsPassword = true
                        openInputDialog = true
                    }
                )
            }
            item {
                CategorySettingsItem(
                    text = stringResource(id = R.string.webdav_screen_sync_category)
                )
            }
            item {
                val webDavIncompleteInfo = remember {
                    {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = appContext.getString(R.string.webdav_screen_info_incomplete),
                                withDismissAction = true
                            )
                        }
                    }
                }
                BaseSettingsItem(
                    icon = rememberVectorPainter(image = Icons.Default.CloudDownload),
                    text = stringResource(id = R.string.webdav_screen_download),
                    descriptionText = stringResource(id = R.string.webdav_screen_download_description),
                    onClick = {
                        if (server.isNotBlank() && account.isNotBlank() && password.isNotBlank()) {
                            viewModel.sendUiIntent(
                                WebDavIntent.StartDownload(
                                    website = server, username = account, password = password
                                )
                            )
                        } else {
                            webDavIncompleteInfo()
                        }
                    }
                )
                BaseSettingsItem(
                    icon = rememberVectorPainter(image = Icons.Default.CloudUpload),
                    text = stringResource(id = R.string.webdav_screen_upload),
                    descriptionText = stringResource(id = R.string.webdav_screen_upload_description),
                    onClick = {
                        if (server.isNotBlank() && account.isNotBlank() && password.isNotBlank()) {
                            viewModel.sendUiIntent(
                                WebDavIntent.StartUpload(
                                    website = server, username = account, password = password
                                )
                            )
                        } else {
                            webDavIncompleteInfo()
                        }
                    }
                )
                BaseSettingsItem(
                    icon = rememberVectorPainter(image = Icons.Default.Recycling),
                    text = stringResource(id = R.string.webdav_screen_remote_recycle_bin),
                    descriptionText = stringResource(id = R.string.webdav_screen_remote_recycle_bin_description),
                    onClick = {
                        if (server.isNotBlank() && account.isNotBlank() && password.isNotBlank()) {
                            viewModel.sendUiIntent(
                                WebDavIntent.GetRemoteRecycleBin(
                                    website = server, username = account, password = password
                                )
                            )
                            openRecycleBinBottomSheet = true
                        } else {
                            webDavIncompleteInfo()
                        }
                    }
                )
            }
        }

        loadUiIntent?.also { loadUiIntent ->
            when (loadUiIntent) {
                is LoadUiIntent.Error -> {
                    snackbarHostState.showSnackbarWithLaunchedEffect(
                        message = appContext.getString(
                            R.string.webdav_screen_failed,
                            loadUiIntent.msg
                        ),
                        key2 = loadUiIntent,
                    )
                    openWaitingDialog = false
                    waitingDialogData = null
                }

                is LoadUiIntent.Loading -> {
                    openWaitingDialog = loadUiIntent.isShow
                    if (!openWaitingDialog) {
                        waitingDialogData = null
                    }
                }
            }
        }

        WaitingDialog(
            visible = openWaitingDialog,
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
                    viewModel.sendUiIntent(
                        WebDavIntent.ClearRemoteRecycleBin(
                            website = server, username = account, password = password,
                        )
                    )
                } else {
                    viewModel.sendUiIntent(
                        WebDavIntent.DeleteFromRemoteRecycleBin(
                            website = server,
                            username = account,
                            password = password,
                            uuid = openDeleteWarningDialog!!
                        )
                    )
                }
                openDeleteWarningDialog = null
            }
        )
        RaysDialog(
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
            title = inputDialogInfo.first,
            value = inputDialogInfo.second,
            maxLines = 1,
            isPassword = inputDialogIsPassword,
            onDismissRequest = { openInputDialog = false },
            onConfirm = inputDialogInfo.third,
            onValueChange = {
                inputDialogInfo = inputDialogInfo.copy(second = it)
            },
        )
        if (openRecycleBinBottomSheet) {
            RecycleBinBottomSheet(
                onDismissRequest = { openRecycleBinBottomSheet = false },
                onRestore = {
                    viewModel.sendUiIntent(
                        WebDavIntent.RestoreFromRemoteRecycleBin(
                            website = server, username = account, password = password, uuid = it
                        )
                    )
                },
                onDelete = { openDeleteWarningDialog = it },
                onClear = { openDeleteWarningDialog = "" }
            )
        }
    }
    uiEvent?.apply {
        when (uploadResultUiEvent) {
            is UploadResultUiEvent.Success -> {
                when (val result = uploadResultUiEvent.result) {
                    is WebDavResultInfo -> {
                        snackbarHostState.showSnackbarWithLaunchedEffect(
                            message = appContext.resources.getQuantityString(
                                R.plurals.webdav_screen_upload_success,
                                result.count,
                                result.time / 1000.0f, result.count
                            ),
                            key2 = result,
                        )
                    }

                    is WebDavWaitingInfo -> {
                        waitingDialogData = result
                    }
                }
            }

            null -> Unit
        }
        when (downloadResultUiEvent) {
            is DownloadResultUiEvent.Success -> {
                when (val result = downloadResultUiEvent.result) {
                    is WebDavResultInfo -> {
                        snackbarHostState.showSnackbarWithLaunchedEffect(
                            message = appContext.resources.getQuantityString(
                                R.plurals.webdav_screen_download_success,
                                result.count,
                                result.time / 1000.0f, result.count
                            ),
                            key2 = result,
                        )
                    }

                    is WebDavWaitingInfo -> {
                        waitingDialogData = result
                    }
                }
            }

            null -> Unit
        }
    }
}

@Composable
private fun RecycleBinBottomSheet(
    onDismissRequest: () -> Unit,
    onRestore: (String) -> Unit,
    onDelete: (String) -> Unit,
    onClear: () -> Unit,
    viewModel: WebDavViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiStateFlow.collectAsStateWithLifecycle()

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
            val getRemoteRecycleBinResultUiState = uiState.getRemoteRecycleBinResultUiState
            if (getRemoteRecycleBinResultUiState is GetRemoteRecycleBinResultUiState.Success &&
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
                                    dateTime(list[it].modifiedTime)
                                )
                            )
                        },
                        trailingContent = {
                            Row {
                                RaysIconButton(
                                    imageVector = Icons.Default.RestoreFromTrash,
                                    contentDescription = stringResource(R.string.webdav_screen_restore),
                                    onClick = { onRestore(list[it].uuid) }
                                )
                                RaysIconButton(
                                    imageVector = Icons.Default.DeleteForever,
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