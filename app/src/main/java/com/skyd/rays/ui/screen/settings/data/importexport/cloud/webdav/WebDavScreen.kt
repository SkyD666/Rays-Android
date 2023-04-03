package com.skyd.rays.ui.screen.settings.data.importexport.cloud.webdav

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.skyd.rays.model.bean.BackupInfo
import com.skyd.rays.model.bean.WebDavResultInfo
import com.skyd.rays.model.bean.WebDavWaitingInfo
import com.skyd.rays.model.preference.WebDavServerPreference
import com.skyd.rays.ui.component.*
import com.skyd.rays.ui.component.dialog.DeleteWarningDialog
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
    var openWaitingDialog by remember { mutableStateOf(false) }
    var waitingDialogCurrent by remember { mutableStateOf<Int?>(null) }
    var waitingDialogTotal by remember { mutableStateOf<Int?>(null) }
    var openDeleteWarningDialog by rememberSaveable { mutableStateOf<String?>(null) }
    var openInputDialog by remember { mutableStateOf(false) }
    var inputDialogInfo by remember {
        mutableStateOf<Triple<String, String, (String) -> Unit>>(Triple("", "") {})
    }
    var inputDialogIsPassword by remember { mutableStateOf(false) }
    var openRecycleBinBottomSheet by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            RaysTopBar(
                style = RaysTopBarStyle.Large,
                scrollBehavior = scrollBehavior,
                title = { Text(text = stringResource(R.string.webdav_screen_name)) },
            )
        }
    ) { paddingValues ->
        val server = LocalWebDavServer.current
        var account by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }

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

        viewModel.loadUiIntentFlow.collectAsStateWithLifecycle(initialValue = null).value?.also { loadUiIntent ->
            when (loadUiIntent) {
                is LoadUiIntent.Error -> {
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = appContext.getString(
                                R.string.webdav_screen_failed,
                                loadUiIntent.msg
                            ),
                            withDismissAction = true
                        )
                    }
                }
                is LoadUiIntent.ShowMainView -> {}
                is LoadUiIntent.Loading -> {
                    openWaitingDialog = loadUiIntent.isShow
                    waitingDialogCurrent = null
                    waitingDialogTotal = null
                }
            }
        }

        WaitingDialog(
            visible = openWaitingDialog,
            currentValue = waitingDialogCurrent,
            totalValue = waitingDialogTotal
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
    viewModel.uiEventFlow.collectAsStateWithLifecycle(initialValue = null).value?.apply {
        when (uploadResultUiEvent) {
            is UploadResultUiEvent.Success -> {
                when (val result = uploadResultUiEvent.result) {
                    is WebDavResultInfo -> {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = appContext.getString(
                                    R.string.webdav_screen_upload_success,
                                    result.time / 1000.0f, result.count
                                ),
                                withDismissAction = true
                            )
                        }
                    }
                    is WebDavWaitingInfo -> {
                        waitingDialogCurrent = result.current
                        waitingDialogTotal = result.total
                    }
                }
            }
            null -> {}
        }
        when (downloadResultUiEvent) {
            is DownloadResultUiEvent.Success -> {
                val state = downloadResultUiEvent.result
                if (state is WebDavResultInfo) {
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = appContext.getString(
                                R.string.webdav_screen_download_success,
                                state.time / 1000.0f, state.count
                            ),
                            withDismissAction = true
                        )
                    }
                } else if (state is WebDavWaitingInfo) {
                    waitingDialogCurrent = state.current
                    waitingDialogTotal = state.total
                }
            }
            null -> {}
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
    var list: List<BackupInfo>

    viewModel.uiStateFlow.collectAsStateWithLifecycle().value.apply {
        list = when (getRemoteRecycleBinResultUiState) {
            GetRemoteRecycleBinResultUiState.Init -> emptyList()
            is GetRemoteRecycleBinResultUiState.Success -> getRemoteRecycleBinResultUiState.result
        }
    }

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
            if (list.isEmpty()) {
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
                            IconButton(onClick = { onRestore(list[it].uuid) }) {
                                Icon(
                                    Icons.Default.RestoreFromTrash,
                                    contentDescription = stringResource(R.string.webdav_screen_restore),
                                )
                            }
                            IconButton(onClick = { onDelete(list[it].uuid) }) {
                                Icon(
                                    Icons.Default.DeleteForever,
                                    contentDescription = stringResource(R.string.webdav_screen_delete),
                                )
                            }
                        }
                    }
                )
                Divider(modifier = Modifier.padding(horizontal = 16.dp))
            }
        }
    }
}