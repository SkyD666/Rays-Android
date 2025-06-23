package com.skyd.rays.ui.screen.settings.data

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cached
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.ImportExport
import androidx.compose.material.icons.outlined.Polyline
import androidx.compose.material.icons.outlined.Source
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skyd.compone.component.ComponeTopBar
import com.skyd.compone.component.ComponeTopBarStyle
import com.skyd.compone.component.dialog.DeleteWarningDialog
import com.skyd.compone.component.dialog.WaitingDialog
import com.skyd.compone.local.LocalNavController
import com.skyd.rays.R
import com.skyd.rays.base.mvi.MviEventListener
import com.skyd.rays.base.mvi.getDispatcher
import com.skyd.rays.ui.screen.settings.data.cache.CacheRoute
import com.skyd.rays.ui.screen.settings.data.importexport.ImportExportRoute
import com.skyd.rays.ui.screen.settings.imagesource.ImageSourceRoute
import com.skyd.settings.BaseSettingsItem
import com.skyd.settings.SettingsLazyColumn
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel


@Serializable
data object DataRoute

@Composable
fun DataScreen(viewModel: DataViewModel = koinViewModel()) {
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
            ComponeTopBar(
                style = ComponeTopBarStyle.LargeFlexible,
                scrollBehavior = scrollBehavior,
                title = { Text(text = stringResource(R.string.data_screen_name)) },
            )
        }
    ) { paddingValues ->
        SettingsLazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = paddingValues,
        ) {
            group {
                item {
                    BaseSettingsItem(
                        icon = rememberVectorPainter(Icons.Outlined.Source),
                        text = stringResource(id = R.string.image_source_screen_name),
                        descriptionText = stringResource(id = R.string.setting_screen_image_source_description),
                        onClick = { navController.navigate(ImageSourceRoute) }
                    )
                }
                item {
                    BaseSettingsItem(
                        icon = rememberVectorPainter(image = Icons.Outlined.ImportExport),
                        text = stringResource(id = R.string.import_export_screen_name),
                        descriptionText = stringResource(id = R.string.data_screen_import_export_description),
                        onClick = { navController.navigate(ImportExportRoute) }
                    )
                }
                item {
                    BaseSettingsItem(
                        icon = rememberVectorPainter(image = Icons.Outlined.Cached),
                        text = stringResource(id = R.string.cache_screen_name),
                        descriptionText = stringResource(id = R.string.cache_screen_description),
                        onClick = { navController.navigate(CacheRoute) }
                    )
                }
                item {
                    BaseSettingsItem(
                        icon = rememberVectorPainter(image = Icons.Outlined.Polyline),
                        text = stringResource(id = R.string.data_screen_delete_vector_db),
                        descriptionText = stringResource(id = R.string.data_screen_delete_vector_db_description),
                        onClick = { dispatch(DataIntent.DeleteVectorDbFiles) }
                    )
                }
            }
            group(text = { context.getString(R.string.data_screen_danger_category) }) {
                item {
                    BaseSettingsItem(
                        icon = rememberVectorPainter(image = Icons.Outlined.Delete),
                        text = stringResource(id = R.string.data_screen_delete_all),
                        descriptionText = stringResource(id = R.string.data_screen_delete_all_description),
                        onClick = { openDeleteAllStickersDialog = true }
                    )
                }
                item {
                    BaseSettingsItem(
                        icon = rememberVectorPainter(image = Icons.Outlined.Delete),
                        text = stringResource(id = R.string.data_screen_delete_all_sticker_share_time_data),
                        descriptionText = stringResource(id = R.string.data_screen_delete_all_sticker_share_time_data_description),
                        onClick = { openDeleteStickerShareTimeDialog = true }
                    )
                }
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

                is DataEvent.DeleteVectorDbFilesResultEvent.Failed -> snackbarHostState.showSnackbar(
                    context.getString(R.string.failed_info, event.msg)
                )

                is DataEvent.DeleteVectorDbFilesResultEvent.Success -> Unit
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
