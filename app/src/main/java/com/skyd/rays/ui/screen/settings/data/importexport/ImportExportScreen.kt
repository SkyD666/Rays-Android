package com.skyd.rays.ui.screen.settings.data.importexport

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.res.stringResource
import com.skyd.rays.R
import com.skyd.rays.ui.component.BaseSettingsItem
import com.skyd.rays.ui.component.CategorySettingsItem
import com.skyd.rays.ui.component.RaysTopBar
import com.skyd.rays.ui.component.RaysTopBarStyle
import com.skyd.rays.ui.local.LocalNavController
import com.skyd.rays.ui.screen.settings.data.importexport.cloud.webdav.WebDavRoute
import com.skyd.rays.ui.screen.settings.data.importexport.file.exportfiles.ExportFilesRoute
import com.skyd.rays.ui.screen.settings.data.importexport.file.importfiles.ImportFilesRoute
import kotlinx.serialization.Serializable


@Serializable
data object ImportExportRoute

@Composable
fun ImportExportScreen() {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val navController = LocalNavController.current

    Scaffold(
        topBar = {
            RaysTopBar(
                style = RaysTopBarStyle.Large,
                scrollBehavior = scrollBehavior,
                title = { Text(text = stringResource(R.string.import_export_screen_name)) },
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
                CategorySettingsItem(
                    text = stringResource(id = R.string.import_export_screen_using_cloud_category)
                )
            }
            item {
                BaseSettingsItem(
                    painter = rememberVectorPainter(image = Icons.Outlined.CloudSync),
                    text = stringResource(id = R.string.webdav_screen_name),
                    descriptionText = stringResource(id = R.string.import_export_screen_webdav_description),
                    onClick = { navController.navigate(WebDavRoute) }
                )
            }
            item {
                CategorySettingsItem(
                    text = stringResource(id = R.string.import_export_screen_using_file_category)
                )
            }
            item {
                BaseSettingsItem(
                    painter = rememberVectorPainter(image = Icons.Outlined.Download),
                    text = stringResource(id = R.string.import_files_screen_name),
                    descriptionText = stringResource(id = R.string.import_files_screen_description),
                    onClick = { navController.navigate(ImportFilesRoute) }
                )
            }
            item {
                BaseSettingsItem(
                    painter = rememberVectorPainter(image = Icons.Outlined.Upload),
                    text = stringResource(id = R.string.export_files_screen_name),
                    descriptionText = stringResource(id = R.string.export_files_screen_description),
                    onClick = { navController.navigate(ExportFilesRoute(exportStickers = null)) }
                )
            }
        }
    }
}
