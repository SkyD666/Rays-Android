package com.skyd.rays.ui.screen.settings.data

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ImportExport
import androidx.compose.material.icons.filled.Source
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skyd.rays.R
import com.skyd.rays.base.mvi.getDispatcher
import com.skyd.rays.ext.showSnackbarWithLaunchedEffect
import com.skyd.rays.ui.component.BaseSettingsItem
import com.skyd.rays.ui.component.RaysTopBar
import com.skyd.rays.ui.component.RaysTopBarStyle
import com.skyd.rays.ui.component.dialog.DeleteWarningDialog
import com.skyd.rays.ui.component.dialog.WaitingDialog
import com.skyd.rays.ui.local.LocalNavController
import com.skyd.rays.ui.screen.settings.data.importexport.IMPORT_EXPORT_SCREEN_ROUTE
import com.skyd.rays.ui.screen.settings.imagesource.IMAGE_SOURCE_SCREEN_ROUTE

const val DATA_SCREEN_ROUTE = "dataScreen"

@Composable
fun DataScreen(viewModel: DataViewModel = hiltViewModel()) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val navController = LocalNavController.current
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var openDeleteWarningDialog by rememberSaveable { mutableStateOf(false) }
    val uiState by viewModel.viewState.collectAsStateWithLifecycle()
    val uiEvent by viewModel.singleEvent.collectAsStateWithLifecycle(initialValue = null)

    val dispatch = viewModel.getDispatcher(startWith = DataIntent.Init)

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
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = paddingValues,
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
            item {
                BaseSettingsItem(
                    icon = rememberVectorPainter(Icons.Default.Source),
                    text = stringResource(id = R.string.image_source_screen_name),
                    descriptionText = stringResource(id = R.string.setting_screen_image_source_description),
                    onClick = { navController.navigate(IMAGE_SOURCE_SCREEN_ROUTE) }
                )
            }
        }

        when (val event = uiEvent) {
            is DataEvent.DeleteAllResultEvent.Success -> {
                snackbarHostState.showSnackbarWithLaunchedEffect(
                    message = context.getString(
                        R.string.data_screen_delete_all_success,
                        event.time / 1000.0f
                    ),
                    key2 = event,
                )
            }

            null -> Unit
        }

        WaitingDialog(visible = uiState.loadingDialog)
        DeleteWarningDialog(
            visible = openDeleteWarningDialog,
            onDismissRequest = { openDeleteWarningDialog = false },
            onDismiss = { openDeleteWarningDialog = false },
            onConfirm = {
                dispatch(DataIntent.DeleteAllData)
                openDeleteWarningDialog = false
            }
        )
    }
}
