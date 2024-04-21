package com.skyd.rays.ui.screen.settings.data.importexport

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Upload
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
import com.skyd.rays.ui.screen.settings.data.importexport.cloud.webdav.WEBDAV_SCREEN_ROUTE
import com.skyd.rays.ui.screen.settings.data.importexport.file.exportfiles.openExportFilesScreen
import com.skyd.rays.ui.screen.settings.data.importexport.file.importfiles.IMPORT_FILES_SCREEN_ROUTE

const val IMPORT_EXPORT_SCREEN_ROUTE = "importExportScreen"

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
                    painter = rememberVectorPainter(image = Icons.Default.CloudSync),
                    text = stringResource(id = R.string.webdav_screen_name),
                    descriptionText = stringResource(id = R.string.import_export_screen_webdav_description),
                    onClick = { navController.navigate(WEBDAV_SCREEN_ROUTE) }
                )
            }
            item {
                CategorySettingsItem(
                    text = stringResource(id = R.string.import_export_screen_using_file_category)
                )
            }
            item {
                BaseSettingsItem(
                    painter = rememberVectorPainter(image = Icons.Default.Download),
                    text = stringResource(id = R.string.import_files_screen_name),
                    descriptionText = stringResource(id = R.string.import_files_screen_description),
                    onClick = { navController.navigate(IMPORT_FILES_SCREEN_ROUTE) }
                )
            }
            item {
                BaseSettingsItem(
                    painter = rememberVectorPainter(image = Icons.Default.Upload),
                    text = stringResource(id = R.string.export_files_screen_name),
                    descriptionText = stringResource(id = R.string.export_files_screen_description),
                    onClick = { openExportFilesScreen(navController = navController) }
                )
            }
        }
    }
}
