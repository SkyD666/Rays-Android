package com.skyd.rays.ui.screen.detail

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.skyd.rays.R
import com.skyd.rays.config.IMAGE_CONTENT_SCALE_HELP_URL
import com.skyd.rays.ui.component.RaysIconButton
import com.skyd.rays.util.CommonUtil.openBrowser


@Composable
fun DetailMenu(
    expanded: Boolean,
    stickerMenuItemEnabled: Boolean,
    onDismissRequest: () -> Unit,
    onDeleteClick: () -> Unit,
    onExportClick: () -> Unit,
    onStickerInfoClick: () -> Unit,
    onStickerScaleClick: () -> Unit,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest
    ) {
        DropdownMenuItem(
            enabled = stickerMenuItemEnabled,
            text = { Text(stringResource(R.string.home_screen_delete)) },
            onClick = {
                onDismissRequest()
                onDeleteClick()
            },
            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) }
        )
        DropdownMenuItem(
            enabled = stickerMenuItemEnabled,
            text = { Text(stringResource(R.string.home_screen_export)) },
            onClick = {
                onDismissRequest()
                onExportClick()
            },
            leadingIcon = { Icon(Icons.Default.Save, contentDescription = null) }
        )
        DropdownMenuItem(
            enabled = stickerMenuItemEnabled,
            text = { Text(stringResource(R.string.detail_screen_sticker_info)) },
            onClick = {
                onDismissRequest()
                onStickerInfoClick()
            },
            leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) }
        )
        HorizontalDivider()
        DropdownMenuItem(
            enabled = stickerMenuItemEnabled,
            text = { Text(stringResource(R.string.detail_screen_sticker_scale)) },
            onClick = {
                onDismissRequest()
                onStickerScaleClick()
            },
            leadingIcon = { Icon(Icons.Default.AspectRatio, contentDescription = null) },
            trailingIcon = {
                RaysIconButton(
                    onClick = { openBrowser(IMAGE_CONTENT_SCALE_HELP_URL) },
                    imageVector = Icons.AutoMirrored.Default.Help,
                )
            }
        )
    }
}