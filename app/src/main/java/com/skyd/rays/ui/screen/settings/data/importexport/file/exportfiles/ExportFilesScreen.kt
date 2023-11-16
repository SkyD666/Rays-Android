package com.skyd.rays.ui.screen.settings.data.importexport.file.exportfiles

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Icon
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
import com.skyd.rays.base.LoadUiIntent
import com.skyd.rays.ext.plus
import com.skyd.rays.ext.showSnackbarWithLaunchedEffect
import com.skyd.rays.model.bean.ImportExportResultInfo
import com.skyd.rays.model.bean.ImportExportWaitingInfo
import com.skyd.rays.ui.component.BaseSettingsItem
import com.skyd.rays.ui.component.RaysExtendedFloatingActionButton
import com.skyd.rays.ui.component.RaysTopBar
import com.skyd.rays.ui.component.RaysTopBarStyle
import com.skyd.rays.ui.component.dialog.RaysDialog
import com.skyd.rays.ui.component.dialog.WaitingDialog


const val EXPORT_FILES_SCREEN_ROUTE = "exportFilesScreen"

@Composable
fun ExportFilesScreen(viewModel: ExportFilesViewModel = hiltViewModel()) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val uiEvent by viewModel.uiEventFlow.collectAsStateWithLifecycle(initialValue = null)
    val loadUiIntent by viewModel.loadUiIntentFlow.collectAsStateWithLifecycle(initialValue = null)
    var openExportDialog by rememberSaveable { mutableStateOf<ImportExportResultInfo?>(null) }
    var openWaitingDialog by rememberSaveable { mutableStateOf(false) }
    var waitingDialogData by rememberSaveable { mutableStateOf<ImportExportWaitingInfo?>(null) }

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
                icon = { Icon(imageVector = Icons.Default.Done, contentDescription = null) },
                onClick = { viewModel.sendUiIntent(ExportFilesIntent.Export(exportDir)) },
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
                    icon = rememberVectorPainter(image = Icons.Default.Folder),
                    text = stringResource(id = R.string.export_files_screen_select_dir),
                    descriptionText = exportDir.toString().ifBlank { null },
                    onClick = { pickExportDirLauncher.launch(exportDir) }
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
                pickExportedFileLauncher.launch(intent)
            }) {
                Text(text = stringResource(id = R.string.export_files_screen_progress_open_backup_file))
            }
        },
        dismissButton = {
            TextButton(onClick = { openExportDialog = null }) {
                Text(text = stringResource(id = R.string.cancel))
            }
        }
    )

    when (val loadUi = loadUiIntent) {
        is LoadUiIntent.Error -> {
            snackbarHostState.showSnackbarWithLaunchedEffect(
                message = context.getString(R.string.failed_info, loadUi.msg),
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
        when (exportResultUiEvent) {
            is ExportResultUiEvent.Success -> {
                when (val result = exportResultUiEvent.info) {
                    is ImportExportResultInfo -> {
                        LaunchedEffect(this) { openExportDialog = result }
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
