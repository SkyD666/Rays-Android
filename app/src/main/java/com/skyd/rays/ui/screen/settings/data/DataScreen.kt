package com.skyd.rays.ui.screen.settings.data

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cached
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
import com.skyd.rays.base.mvi.MviEventListener
import com.skyd.rays.base.mvi.getDispatcher
import com.skyd.rays.ui.component.BaseSettingsItem
import com.skyd.rays.ui.component.CategorySettingsItem
import com.skyd.rays.ui.component.RaysTopBar
import com.skyd.rays.ui.component.RaysTopBarStyle
import com.skyd.rays.ui.component.dialog.DeleteWarningDialog
import com.skyd.rays.ui.component.dialog.WaitingDialog
import com.skyd.rays.ui.local.LocalNavController
import com.skyd.rays.ui.screen.settings.data.cache.CACHE_SCREEN_ROUTE
import com.skyd.rays.ui.screen.settings.data.importexport.IMPORT_EXPORT_SCREEN_ROUTE
import com.skyd.rays.ui.screen.settings.imagesource.IMAGE_SOURCE_SCREEN_ROUTE

const val DATA_SCREEN_ROUTE = "dataScreen"

@Composable
fun DataScreen(viewModel: DataViewModel = hiltViewModel()) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val navController = LocalNavController.current
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var openDeleteAllStickersDialog by rememberSaveable { mutableStateOf(false) }
    var openDeleteStickerShareTimeDialog by rememberSaveable { mutableStateOf(false) }
    val uiState by viewModel.viewState.collectAsStateWithLifecycle()
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
                    painter = rememberVectorPainter(Icons.Default.Source),
                    text = stringResource(id = R.string.image_source_screen_name),
                    descriptionText = stringResource(id = R.string.setting_screen_image_source_description),
                    onClick = { navController.navigate(IMAGE_SOURCE_SCREEN_ROUTE) }
                )
            }
            item {
                BaseSettingsItem(
                    painter = rememberVectorPainter(image = Icons.Default.ImportExport),
                    text = stringResource(id = R.string.import_export_screen_name),
                    descriptionText = stringResource(id = R.string.data_screen_import_export_description),
                    onClick = { navController.navigate(IMPORT_EXPORT_SCREEN_ROUTE) }
                )
            }
            item {
                BaseSettingsItem(
                    painter = rememberVectorPainter(image = Icons.Default.Cached),
                    text = stringResource(id = R.string.cache_screen_name),
                    descriptionText = stringResource(id = R.string.cache_screen_description),
                    onClick = { navController.navigate(CACHE_SCREEN_ROUTE) }
                )
            }
            item {
                CategorySettingsItem(text = stringResource(id = R.string.data_screen_danger_category))
            }
            item {
                BaseSettingsItem(
                    painter = rememberVectorPainter(image = Icons.Default.Delete),
                    text = stringResource(id = R.string.data_screen_delete_all),
                    descriptionText = stringResource(id = R.string.data_screen_delete_all_description),
                    onClick = { openDeleteAllStickersDialog = true }
                )
            }
            item {
                BaseSettingsItem(
                    painter = rememberVectorPainter(image = Icons.Default.Delete),
                    text = stringResource(id = R.string.data_screen_delete_all_sticker_share_time_data),
                    descriptionText = stringResource(id = R.string.data_screen_delete_all_sticker_share_time_data_description),
                    onClick = { openDeleteStickerShareTimeDialog = true }
                )
            }
        }

        MviEventListener(viewModel.singleEvent) { event ->
            when (event) {
                is DataEvent.DeleteAllResultEvent.Success -> snackbarHostState.showSnackbar(
                    context.getString(
                        R.string.data_screen_delete_all_success,
                        event.time / 1000.0f
                    ),
                )

                is DataEvent.DeleteStickerShareTimeResultEvent.Success -> snackbarHostState.showSnackbar(
                    context.getString(
                        R.string.data_screen_delete_sticker_share_time_success,
                        event.time / 1000.0f
                    ),
                )
            }
        }

        WaitingDialog(visible = uiState.loadingDialog)
        DeleteWarningDialog(
            visible = openDeleteAllStickersDialog,
            onDismissRequest = { openDeleteAllStickersDialog = false },
            onDismiss = { openDeleteAllStickersDialog = false },
            onConfirm = {
                dispatch(DataIntent.DeleteAllData)
                openDeleteAllStickersDialog = false
            }
        )
        DeleteWarningDialog(
            visible = openDeleteStickerShareTimeDialog,
            onDismissRequest = { openDeleteStickerShareTimeDialog = false },
            onDismiss = { openDeleteStickerShareTimeDialog = false },
            onConfirm = {
                dispatch(DataIntent.DeleteStickerShareTime)
                openDeleteStickerShareTimeDialog = false
            }
        )
    }
}
