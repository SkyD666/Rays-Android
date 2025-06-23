package com.skyd.rays.ui.screen.settings.data.importexport

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudSync
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.skyd.compone.component.ComponeTopBar
import com.skyd.compone.component.ComponeTopBarStyle
import com.skyd.compone.local.LocalNavController
import com.skyd.rays.R
import com.skyd.rays.ui.screen.settings.data.importexport.cloud.webdav.WebDavRoute
import com.skyd.rays.ui.screen.settings.data.importexport.file.exportfiles.ExportFilesRoute
import com.skyd.rays.ui.screen.settings.data.importexport.file.importfiles.ImportFilesRoute
import com.skyd.settings.BaseSettingsItem
import com.skyd.settings.SettingsLazyColumn
import kotlinx.serialization.Serializable


@Serializable
data object ImportExportRoute

@Composable
fun ImportExportScreen() {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val navController = LocalNavController.current
    val context = LocalContext.current

    Scaffold(
        topBar = {
            ComponeTopBar(
                style = ComponeTopBarStyle.LargeFlexible,
                scrollBehavior = scrollBehavior,
                title = { Text(text = stringResource(R.string.import_export_screen_name)) },
            )
        }
    ) { paddingValues ->
        SettingsLazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = paddingValues,
        ) {
            group(text = { context.getString(R.string.import_export_screen_using_cloud_category) }) {
                item {
                    BaseSettingsItem(
                        icon = rememberVectorPainter(image = Icons.Outlined.CloudSync),
                        text = stringResource(id = R.string.webdav_screen_name),
                        descriptionText = stringResource(id = R.string.import_export_screen_webdav_description),
                        onClick = { navController.navigate(WebDavRoute) }
                    )
                }
            }
            group(text = { context.getString(R.string.import_export_screen_using_file_category) }) {
                item {
                    BaseSettingsItem(
                        icon = rememberVectorPainter(image = Icons.Outlined.Download),
                        text = stringResource(id = R.string.import_files_screen_name),
                        descriptionText = stringResource(id = R.string.import_files_screen_description),
                        onClick = { navController.navigate(ImportFilesRoute) }
                    )
                }
                item {
                    BaseSettingsItem(
                        icon = rememberVectorPainter(image = Icons.Outlined.Upload),
                        text = stringResource(id = R.string.export_files_screen_name),
                        descriptionText = stringResource(id = R.string.export_files_screen_description),
                        onClick = { navController.navigate(ExportFilesRoute(exportStickers = null)) }
                    )
                }
            }
        }
    }
}
