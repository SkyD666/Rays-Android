package com.skyd.rays.ui.screen.settings.data.importexport.file.importfiles

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material.icons.outlined.FolderZip
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skyd.compone.component.ComponeExtendedFloatingActionButton
import com.skyd.compone.component.ComponeTopBar
import com.skyd.compone.component.ComponeTopBarStyle
import com.skyd.compone.component.dialog.ComponeDialog
import com.skyd.compone.component.dialog.WaitingDialog
import com.skyd.rays.R
import com.skyd.rays.base.mvi.MviEventListener
import com.skyd.rays.base.mvi.getDispatcher
import com.skyd.rays.ext.safeLaunch
import com.skyd.rays.model.bean.ImportExportResultInfo
import com.skyd.rays.model.bean.ImportExportWaitingInfo
import com.skyd.rays.model.db.dao.sticker.HandleImportedStickerStrategy
import com.skyd.settings.BaseSettingsItem
import com.skyd.settings.SettingsLazyColumn
import com.skyd.settings.TipSettingsItem
import com.skyd.settings.plus
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel


@Serializable
data object ImportFilesRoute

@Composable
fun ImportFilesScreen(viewModel: ImportFilesViewModel = koinViewModel()) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val uiState by viewModel.viewState.collectAsStateWithLifecycle()
    var openImportDialog by rememberSaveable { mutableStateOf<ImportExportResultInfo?>(null) }
    var waitingDialogData by rememberSaveable { mutableStateOf<ImportExportWaitingInfo?>(null) }
    val importedStickerProxyList = rememberSaveable {
        listOf(
            HandleImportedStickerStrategy.SkipStrategy,
            HandleImportedStickerStrategy.ReplaceStrategy
        )
    }
    var selectedImportedStickerProxyIndex by rememberSaveable { mutableIntStateOf(0) }

    val dispatch = viewModel.getDispatcher(startWith = ImportFilesIntent.Init)

    var fileUri by rememberSaveable { mutableStateOf(Uri.EMPTY) }
    val pickFileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            fileUri = uri
        }
    }
    val lazyListState = rememberLazyListState()
    var fabHeight by remember { mutableStateOf(0.dp) }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            ComponeTopBar(
                style = ComponeTopBarStyle.LargeFlexible,
                scrollBehavior = scrollBehavior,
                title = { Text(text = stringResource(R.string.import_files_screen_name)) },
            )
        },
        floatingActionButton = {
            ComponeExtendedFloatingActionButton(
                text = { Text(text = stringResource(R.string.import_files_screen_import)) },
                icon = { Icon(imageVector = Icons.Outlined.Done, contentDescription = null) },
                onClick = {
                    dispatch(
                        ImportFilesIntent.Import(
                            backupFileUri = fileUri,
                            strategy = importedStickerProxyList[selectedImportedStickerProxyIndex],
                        )
                    )
                },
                onSizeWithSinglePaddingChanged = { _, height -> fabHeight = height },
                contentDescription = stringResource(R.string.import_files_screen_import)
            )
        },
    ) { paddingValues ->
        SettingsLazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = paddingValues + PaddingValues(bottom = fabHeight),
            state = lazyListState,
        ) {
            group {
                item {
                    BaseSettingsItem(
                        icon = rememberVectorPainter(image = Icons.Outlined.FolderZip),
                        text = stringResource(id = R.string.import_files_screen_select_file),
                        descriptionText = fileUri.toString().ifBlank { null },
                        onClick = { pickFileLauncher.safeLaunch("application/zip") }
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
            item {
                TipSettingsItem(
                    text = stringResource(id = R.string.import_files_screen_on_conflict_desc)
                )
            }
        }
    }

    WaitingDialog(
        visible = uiState.loadingDialog,
        currentValue = waitingDialogData?.current,
        totalValue = waitingDialogData?.total,
        msg = waitingDialogData?.msg.orEmpty() + "\n\n" + stringResource(id = R.string.data_sync_warning),
    )

    ComponeDialog(
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

    MviEventListener(viewModel.singleEvent) { event ->
        when (event) {
            is ImportFilesEvent.ImportResultEvent.Success -> openImportDialog = event.info
            is ImportFilesEvent.ImportResultEvent.Error -> snackbarHostState.showSnackbar(
                context.getString(R.string.failed_info, event.msg),
            )
        }
    }

    waitingDialogData = when (val state = uiState.importProgressEvent) {
        ImportProgressState.None -> null
        is ImportProgressState.Progress -> state.info
    }
}
