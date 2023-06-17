package com.skyd.rays.ui.screen.settings.data

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ImportExport
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
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skyd.rays.R
import com.skyd.rays.appContext
import com.skyd.rays.base.LoadUiIntent
import com.skyd.rays.ui.component.BaseSettingsItem
import com.skyd.rays.ui.component.RaysTopBar
import com.skyd.rays.ui.component.RaysTopBarStyle
import com.skyd.rays.ui.component.dialog.DeleteWarningDialog
import com.skyd.rays.ui.component.dialog.WaitingDialog
import com.skyd.rays.ui.local.LocalNavController
import com.skyd.rays.ui.screen.settings.data.importexport.IMPORT_EXPORT_SCREEN_ROUTE
import kotlinx.coroutines.launch

const val DATA_SCREEN_ROUTE = "dataScreen"

@Composable
fun DataScreen(viewModel: DataViewModel = hiltViewModel()) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val navController = LocalNavController.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var openDeleteWarningDialog by rememberSaveable { mutableStateOf(false) }
    var openWaitingDialog by rememberSaveable { mutableStateOf(false) }
    val uiEvent by viewModel.uiEventFlow.collectAsStateWithLifecycle(initialValue = null)
    val loadUiIntent by viewModel.loadUiIntentFlow.collectAsStateWithLifecycle(initialValue = null)

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            RaysTopBar(
                style = RaysTopBarStyle.Large,
                scrollBehavior = scrollBehavior,
                title = { Text(text = stringResource(R.string.data_screen_name)) },
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection), contentPadding = paddingValues
        ) {
            item {
                BaseSettingsItem(
                    icon = rememberVectorPainter(image = Icons.Default.ImportExport),
                    text = stringResource(id = R.string.import_export_screen_name),
                    descriptionText = stringResource(id = R.string.data_screen_import_export_description),
                    onClick = { navController.navigate(IMPORT_EXPORT_SCREEN_ROUTE) }
                )
                BaseSettingsItem(
                    icon = rememberVectorPainter(image = Icons.Default.Delete),
                    text = stringResource(id = R.string.data_screen_delete_all),
                    descriptionText = stringResource(id = R.string.data_screen_delete_all_description),
                    onClick = { openDeleteWarningDialog = true }
                )
            }
        }

        loadUiIntent?.also { loadUiIntent ->
            when (loadUiIntent) {
                is LoadUiIntent.Error -> {
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = appContext.getString(
                                R.string.data_screen_failed, loadUiIntent.msg
                            ),
                            withDismissAction = true
                        )
                    }
                }

                is LoadUiIntent.Loading -> {
                    openWaitingDialog = loadUiIntent.isShow
                }
            }
        }
        uiEvent?.apply {
            when (deleteAllResultUiEvent) {
                is DeleteAllResultUiEvent.Success -> {
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = appContext.getString(
                                R.string.data_screen_delete_all_success,
                                deleteAllResultUiEvent.time / 1000.0f
                            ),
                            withDismissAction = true
                        )
                    }
                }

                null -> Unit
            }
        }

        WaitingDialog(visible = openWaitingDialog)
        DeleteWarningDialog(
            visible = openDeleteWarningDialog,
            onDismissRequest = { openDeleteWarningDialog = false },
            onDismiss = { openDeleteWarningDialog = false },
            onConfirm = {
                viewModel.sendUiIntent(DataIntent.Start)
                openDeleteWarningDialog = false
            }
        )
    }
}
