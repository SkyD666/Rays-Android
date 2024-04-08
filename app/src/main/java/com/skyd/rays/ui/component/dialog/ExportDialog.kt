package com.skyd.rays.ui.component.dialog

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.skyd.rays.R
import com.skyd.rays.ext.safeLaunch
import com.skyd.rays.model.preference.ExportStickerDirPreference
import com.skyd.rays.ui.component.RaysIconButton
import com.skyd.rays.ui.local.LocalExportStickerDir


@Composable
fun ExportDialog(
    visible: Boolean,
    onDismissRequest: () -> Unit = {},
    onExport: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val exportStickerDir = LocalExportStickerDir.current
    val pickExportDirLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            context.contentResolver.takePersistableUriPermission(
                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            ExportStickerDirPreference.put(
                context = context,
                scope = scope,
                value = uri.toString()
            )
        }
    }
    RaysDialog(
        visible = visible,
        title = { Text(text = stringResource(R.string.home_screen_export)) },
        text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = exportStickerDir.ifBlank {
                        stringResource(id = R.string.home_screen_select_export_folder_tip)
                    }
                )
                RaysIconButton(
                    onClick = {
                        pickExportDirLauncher.safeLaunch(Uri.parse(exportStickerDir))
                    },
                    imageVector = Icons.Default.Folder,
                    contentDescription = stringResource(R.string.home_screen_select_export_folder)
                )
            }
        },
        onDismissRequest = onDismissRequest,
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = stringResource(id = R.string.cancel))
            }
        },
        confirmButton = {
            TextButton(
                enabled = exportStickerDir.isNotBlank(),
                onClick = {
                    onDismissRequest()
                    onExport()
                }
            ) {
                Text(text = stringResource(id = R.string.dialog_ok))
            }
        }
    )
}