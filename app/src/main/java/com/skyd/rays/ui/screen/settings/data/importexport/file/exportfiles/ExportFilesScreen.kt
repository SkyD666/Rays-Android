package com.skyd.rays.ui.screen.settings.data.importexport.file.exportfiles

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.LayersClear
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.skyd.rays.R
import com.skyd.rays.base.mvi.MviEventListener
import com.skyd.rays.base.mvi.getDispatcher
import com.skyd.rays.ext.navigate
import com.skyd.rays.ext.plus
import com.skyd.rays.ext.safeLaunch
import com.skyd.rays.ext.showSnackbar
import com.skyd.rays.model.bean.ImportExportResultInfo
import com.skyd.rays.model.bean.ImportExportWaitingInfo
import com.skyd.rays.ui.component.BaseSettingsItem
import com.skyd.rays.ui.component.RaysExtendedFloatingActionButton
import com.skyd.rays.ui.component.RaysTopBar
import com.skyd.rays.ui.component.RaysTopBarStyle
import com.skyd.rays.ui.component.dialog.MultiChoiceDialog
import com.skyd.rays.ui.component.dialog.RaysDialog
import com.skyd.rays.ui.component.dialog.WaitingDialog


const val EXPORT_FILES_SCREEN_ROUTE = "exportFilesScreen"

fun openExportFilesScreen(
    navController: NavHostController,
    exportStickers: List<String>? = null,
) {
    navController.navigate(
        EXPORT_FILES_SCREEN_ROUTE,
        Bundle().apply {
            if (exportStickers != null) {
                putStringArrayList("exportStickers", ArrayList(exportStickers))
            }
        }
    )
}

@Composable
fun ExportFilesScreen(
    exportStickers: List<String>? = null,
    viewModel: ExportFilesViewModel = hiltViewModel()
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val uiState by viewModel.viewState.collectAsStateWithLifecycle()
    var openMultiChoiceDialog by rememberSaveable { mutableStateOf(false) }
    var openExportDialog by rememberSaveable { mutableStateOf<ImportExportResultInfo?>(null) }
    var waitingDialogData by rememberSaveable { mutableStateOf<ImportExportWaitingInfo?>(null) }

    val dispatch = viewModel.getDispatcher(startWith = ExportFilesIntent.Init)

    val pickExportedFileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { }

    var exportDir by rememberSaveable { mutableStateOf(Uri.EMPTY) }
    val pickExportDirLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            exportDir = uri
        }
    }
    val lazyListState = rememberLazyListState()
    var fabHeight by remember { mutableStateOf(0.dp) }

    val excludeCheckedList = remember { mutableStateListOf<Int>() }
    val excludeOptions = listOf(
        stringResource(R.string.sticker_click_count),
        stringResource(R.string.sticker_share_count),
        stringResource(R.string.sticker_create_time),
        stringResource(R.string.sticker_modify_time),
    )

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            RaysTopBar(
                style = RaysTopBarStyle.Large,
                scrollBehavior = scrollBehavior,
                title = { Text(text = stringResource(R.string.export_files_screen_name)) },
            )
        },
        floatingActionButton = {
            RaysExtendedFloatingActionButton(
                text = { Text(text = stringResource(R.string.export_files_screen_export)) },
                icon = { Icon(imageVector = Icons.Outlined.Done, contentDescription = null) },
                onClick = {
                    if (exportStickers != null && exportStickers.isEmpty()) {
                        snackbarHostState.showSnackbar(
                            scope = scope,
                            message = context.getString(R.string.export_files_screen_no_stickers_to_export),
                        )
                    } else {
                        dispatch(
                            ExportFilesIntent.Export(
                                dirUri = exportDir,
                                excludeClickCount = 0 in excludeCheckedList,
                                excludeShareCount = 1 in excludeCheckedList,
                                excludeCreateTime = 2 in excludeCheckedList,
                                excludeModifyTime = 3 in excludeCheckedList,
                                exportStickers = exportStickers,
                            )
                        )
                    }
                },
                onSizeWithSinglePaddingChanged = { _, height -> fabHeight = height },
                contentDescription = stringResource(R.string.export_files_screen_export)
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = paddingValues + PaddingValues(bottom = fabHeight),
            state = lazyListState,
        ) {
            item {
                BaseSettingsItem(
                    painter = rememberVectorPainter(image = Icons.Outlined.Folder),
                    text = stringResource(id = R.string.export_files_screen_select_dir),
                    descriptionText = exportDir.toString().ifBlank { null },
                    onClick = { pickExportDirLauncher.safeLaunch(exportDir) }
                )
            }
            item {
                BaseSettingsItem(
                    painter = rememberVectorPainter(image = Icons.Outlined.LayersClear),
                    text = stringResource(R.string.export_files_screen_exclude_field),
                    descriptionText = excludeOptions.filterIndexed { index, _ ->
                        index in excludeCheckedList
                    }.joinToString(separator = ", ").ifBlank { null },
                    onClick = { openMultiChoiceDialog = true },
                )
            }
        }
    }

    MultiChoiceDialog(
        visible = openMultiChoiceDialog,
        onDismissRequest = { openMultiChoiceDialog = false },
        title = { Text(text = stringResource(R.string.export_files_screen_exclude_field)) },
        options = excludeOptions,
        checkedIndexList = excludeCheckedList,
        onConfirm = {
            openMultiChoiceDialog = false
            excludeCheckedList.clear()
            excludeCheckedList.addAll(it)
        },
    )

    WaitingDialog(
        visible = uiState.loadingDialog,
        currentValue = waitingDialogData?.current,
        totalValue = waitingDialogData?.total,
        msg = waitingDialogData?.msg.orEmpty() + "\n\n" + stringResource(id = R.string.data_sync_warning),
    )

    RaysDialog(
        visible = openExportDialog != null,
        title = { Text(text = stringResource(id = R.string.export_files_screen_successful)) },
        text = {
            openExportDialog?.let { info ->
                Text(
                    text = context.resources.getQuantityString(
                        R.plurals.export_files_screen_successful_desc,
                        info.count,
                        info.count,
                        info.time / 1000f,
                    )
                )
            }
        },
        onDismissRequest = { openExportDialog = null },
        confirmButton = {
            TextButton(onClick = {
                val intent = Intent.createChooser(
                    Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(openExportDialog!!.backupFile, "application/zip")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    },
                    context.getString(R.string.export_files_screen_open_backup_file)
                )
                pickExportedFileLauncher.safeLaunch(intent)
            }) {
                Text(text = stringResource(id = R.string.export_files_screen_progress_open_backup_file))
            }
        },
        dismissButton = {
            TextButton(onClick = { openExportDialog = null }) {
                Text(text = stringResource(id = R.string.dialog_ok))
            }
        }
    )

    MviEventListener(viewModel.singleEvent) { event ->
        when (event) {
            is ExportFilesEvent.ExportResultEvent.Success -> openExportDialog = event.info
            is ExportFilesEvent.ExportResultEvent.Error -> snackbarHostState.showSnackbar(
                context.getString(R.string.failed_info, event.msg),
            )
        }
    }

    waitingDialogData = when (val state = uiState.exportProgressEvent) {
        ExportProgressState.None -> null
        is ExportProgressState.Progress -> state.info
    }
}
