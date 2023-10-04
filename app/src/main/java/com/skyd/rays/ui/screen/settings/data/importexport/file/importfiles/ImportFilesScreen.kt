package com.skyd.rays.ui.screen.settings.data.importexport.file.importfiles

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skyd.rays.R
import com.skyd.rays.appContext
import com.skyd.rays.base.LoadUiIntent
import com.skyd.rays.ext.inBottomOrNotLarge
import com.skyd.rays.ext.showSnackbarWithLaunchedEffect
import com.skyd.rays.model.bean.ImportExportResultInfo
import com.skyd.rays.model.bean.ImportExportWaitingInfo
import com.skyd.rays.model.db.dao.sticker.HandleImportedStickerProxy
import com.skyd.rays.ui.component.BaseSettingsItem
import com.skyd.rays.ui.component.BottomHideExtendedFloatingActionButton
import com.skyd.rays.ui.component.RaysTopBar
import com.skyd.rays.ui.component.RaysTopBarStyle
import com.skyd.rays.ui.component.dialog.RaysDialog
import com.skyd.rays.ui.component.dialog.WaitingDialog

const val IMPORT_FILES_SCREEN_ROUTE = "importFilesScreen"

@Composable
fun ImportFilesScreen(viewModel: ImportFilesViewModel = hiltViewModel()) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val uiEvent by viewModel.uiEventFlow.collectAsStateWithLifecycle(initialValue = null)
    val loadUiIntent by viewModel.loadUiIntentFlow.collectAsStateWithLifecycle(initialValue = null)
    var openImportDialog by rememberSaveable { mutableStateOf<ImportExportResultInfo?>(null) }
    var openWaitingDialog by rememberSaveable { mutableStateOf(false) }
    var waitingDialogData by rememberSaveable { mutableStateOf<ImportExportWaitingInfo?>(null) }
    val importedStickerProxyList = rememberSaveable {
        listOf(HandleImportedStickerProxy.SkipProxy, HandleImportedStickerProxy.ReplaceProxy)
    }
    var selectedImportedStickerProxyIndex by rememberSaveable { mutableIntStateOf(0) }

    var fileUri by rememberSaveable { mutableStateOf(Uri.EMPTY) }
    val pickFileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            fileUri = uri
        }
    }
    val lazyListState = rememberLazyListState()
    val fabVisibility by remember {
        derivedStateOf {
            lazyListState.inBottomOrNotLarge && fileUri.toString().isNotBlank()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            RaysTopBar(
                style = RaysTopBarStyle.Large,
                scrollBehavior = scrollBehavior,
                title = { Text(text = stringResource(R.string.import_files_screen_name)) },
            )
        },
        floatingActionButton = {
            BottomHideExtendedFloatingActionButton(
                visible = fabVisibility,
                text = { Text(text = stringResource(R.string.import_files_screen_import)) },
                icon = { Icon(imageVector = Icons.Default.Done, contentDescription = null) },
                onClick = {
                    viewModel.sendUiIntent(
                        ImportFilesIntent.Import(
                            backupFileUri = fileUri,
                            proxy = importedStickerProxyList[selectedImportedStickerProxyIndex],
                        )
                    )
                },
                contentDescription = stringResource(R.string.import_files_screen_import)
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = paddingValues,
            state = lazyListState,
        ) {
            item {
                BaseSettingsItem(
                    icon = rememberVectorPainter(image = Icons.Default.FolderZip),
                    text = stringResource(id = R.string.import_files_screen_select_file),
                    descriptionText = fileUri.toString().ifBlank { null },
                    onClick = { pickFileLauncher.launch("application/zip") }
                )
            }
            item {
                BaseSettingsItem(
                    icon = rememberVectorPainter(image = Icons.AutoMirrored.Filled.Help),
                    text = stringResource(R.string.import_files_screen_on_conflict),
                    description = {
                        SingleChoiceSegmentedButtonRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                        ) {
                            importedStickerProxyList.forEachIndexed { index, proxy ->
                                SegmentedButton(
                                    shape = SegmentedButtonDefaults.itemShape(
                                        index = index,
                                        count = importedStickerProxyList.size
                                    ),
                                    onClick = { selectedImportedStickerProxyIndex = index },
                                    selected = index == selectedImportedStickerProxyIndex
                                ) {
                                    Text(text = proxy.displayName)
                                }
                            }
                        }
                    }
                )
            }
        }
    }

    WaitingDialog(
        visible = openWaitingDialog,
        currentValue = waitingDialogData?.current,
        totalValue = waitingDialogData?.total,
        msg = waitingDialogData?.msg + "\n\n" + stringResource(id = R.string.data_sync_warning),
    )

    RaysDialog(
        visible = openImportDialog != null,
        title = { Text(text = stringResource(id = R.string.import_files_screen_successful)) },
        text = {
            openImportDialog?.let { info ->
                Text(
                    text = context.resources.getQuantityString(
                        R.plurals.import_files_screen_successful_desc,
                        info.count,
                        info.count,
                        info.time / 1000f,
                    )
                )
            }
        },
        onDismissRequest = { openImportDialog = null },
        confirmButton = {
            TextButton(onClick = { openImportDialog = null }) {
                Text(text = stringResource(id = R.string.dialog_ok))
            }
        }
    )

    when (val loadUi = loadUiIntent) {
        is LoadUiIntent.Error -> {
            snackbarHostState.showSnackbarWithLaunchedEffect(
                message = appContext.getString(R.string.import_files_screen_failed, loadUi.msg),
                key2 = loadUiIntent,
            )
            openWaitingDialog = false
            waitingDialogData = null
        }

        is LoadUiIntent.Loading -> {
            openWaitingDialog = loadUi.isShow
            if (!openWaitingDialog) {
                waitingDialogData = null
            }
        }

        null -> Unit
    }

    uiEvent?.apply {
        when (importResultUiEvent) {
            is ImportResultUiEvent.Success -> {
                when (val result = importResultUiEvent.info) {
                    is ImportExportResultInfo -> {
                        LaunchedEffect(this) { openImportDialog = result }
                    }

                    is ImportExportWaitingInfo -> {
                        waitingDialogData = result
                    }
                }
            }

            null -> Unit
        }
    }
}
