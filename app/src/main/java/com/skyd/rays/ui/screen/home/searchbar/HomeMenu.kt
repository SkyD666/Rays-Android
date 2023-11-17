package com.skyd.rays.ui.screen.home.searchbar

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ManageSearch
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.skyd.rays.R
import com.skyd.rays.model.bean.UriWithStickerUuidBean
import com.skyd.rays.model.preference.search.SearchResultSortPreference
import com.skyd.rays.ui.local.LocalCurrentStickerUuid
import com.skyd.rays.ui.local.LocalNavController
import com.skyd.rays.ui.local.LocalSearchResultSort
import com.skyd.rays.ui.screen.add.openAddScreen
import com.skyd.rays.ui.screen.settings.searchconfig.SEARCH_CONFIG_SCREEN_ROUTE
import com.skyd.rays.util.stickerUuidToUri


@Composable
internal fun SearchResultSortMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val searchResultSort = LocalSearchResultSort.current

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest
    ) {
        SearchResultSortPreference.sortList.forEach {
            DropdownMenuItem(
                text = { Text(text = SearchResultSortPreference.toDisplayName(it)) },
                leadingIcon = {
                    if (searchResultSort == it) {
                        Icon(
                            imageVector = Icons.Default.Done,
                            contentDescription = null
                        )
                    }
                },
                onClick = {
                    SearchResultSortPreference.put(
                        context = context,
                        scope = scope,
                        value = it
                    )
                    onDismissRequest()
                },
            )
        }
    }
}

@Composable
fun HomeMenu(
    expanded: Boolean,
    stickerMenuItemEnabled: Boolean,
    onDismissRequest: () -> Unit,
    onDeleteClick: () -> Unit,
    onExportClick: () -> Unit,
    onCopyClick: () -> Unit,
    onStickerInfoClick: () -> Unit,
    onClearScreen: () -> Unit,
) {
    val navController = LocalNavController.current
    val currentStickerUuid = LocalCurrentStickerUuid.current

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest
    ) {
        DropdownMenuItem(
            enabled = stickerMenuItemEnabled,
            text = { Text(stringResource(R.string.home_screen_clear_current_sicker)) },
            onClick = {
                onClearScreen()
                onDismissRequest()
            },
            leadingIcon = {
                Icon(
                    Icons.Default.Replay,
                    contentDescription = null
                )
            }
        )
        DropdownMenuItem(
            enabled = stickerMenuItemEnabled,
            text = { Text(stringResource(R.string.home_screen_edit)) },
            onClick = {
                openAddScreen(
                    navController = navController,
                    stickers = mutableListOf(
                        UriWithStickerUuidBean(
                            uri = stickerUuidToUri(currentStickerUuid),
                            stickerUuid = currentStickerUuid,
                        )
                    ),
                    isEdit = true
                )
                onDismissRequest()
            },
            leadingIcon = {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = null
                )
            }
        )
        DropdownMenuItem(
            enabled = stickerMenuItemEnabled,
            text = { Text(stringResource(R.string.home_screen_delete)) },
            onClick = {
                onDismissRequest()
                onDeleteClick()
            },
            leadingIcon = {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null
                )
            }
        )
        DropdownMenuItem(
            enabled = stickerMenuItemEnabled,
            text = { Text(stringResource(R.string.home_screen_export)) },
            onClick = {
                onDismissRequest()
                onExportClick()
            },
            leadingIcon = {
                Icon(
                    Icons.Default.Save,
                    contentDescription = null
                )
            }
        )
        DropdownMenuItem(
            enabled = stickerMenuItemEnabled,
            text = { Text(stringResource(R.string.home_screen_copy)) },
            onClick = {
                onDismissRequest()
                onCopyClick()
            },
            leadingIcon = {
                Icon(
                    Icons.Default.ContentCopy,
                    contentDescription = null
                )
            }
        )
        DropdownMenuItem(
            enabled = stickerMenuItemEnabled,
            text = { Text(stringResource(R.string.home_screen_sticker_info)) },
            onClick = {
                onDismissRequest()
                onStickerInfoClick()
            },
            leadingIcon = {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null
                )
            }
        )
        HorizontalDivider()
        DropdownMenuItem(
            text = { Text(stringResource(R.string.search_config_screen_name)) },
            onClick = {
                navController.navigate(SEARCH_CONFIG_SCREEN_ROUTE)
                onDismissRequest()
            },
            leadingIcon = {
                Icon(
                    Icons.AutoMirrored.Default.ManageSearch,
                    contentDescription = null
                )
            }
        )
    }
}