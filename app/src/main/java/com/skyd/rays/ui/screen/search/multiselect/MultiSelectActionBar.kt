package com.skyd.rays.ui.screen.search.multiselect

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FolderZip
import androidx.compose.material.icons.outlined.ImageSearch
import androidx.compose.material.icons.outlined.Merge
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Share
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skyd.compone.component.ComponeIconButton
import com.skyd.compone.component.dialog.DeleteWarningDialog
import com.skyd.compone.component.dialog.WaitingDialog
import com.skyd.compone.local.LocalNavController
import com.skyd.rays.R
import com.skyd.rays.base.mvi.MviEventListener
import com.skyd.rays.base.mvi.getDispatcher
import com.skyd.rays.ext.isCompact
import com.skyd.rays.model.bean.UriWithStickerUuidBean
import com.skyd.rays.ui.component.dialog.ExportDialog
import com.skyd.rays.ui.component.showToast
import com.skyd.rays.ui.local.LocalWindowSizeClass
import com.skyd.rays.ui.screen.add.AddRoute
import com.skyd.rays.ui.screen.mergestickers.MergeStickersRoute
import com.skyd.rays.ui.screen.search.imagesearch.ImageSearchRoute
import com.skyd.rays.ui.screen.settings.data.importexport.file.exportfiles.ExportFilesRoute
import com.skyd.rays.util.stickerUuidToUri
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MultiSelectActionBar(
    modifier: Modifier = Modifier,
    selectedStickers: Collection<String>,
    contentPadding: PaddingValues = PaddingValues(),
    onRemoveSelectedStickers: (Collection<String>) -> Unit,
    onMessage: (String) -> Unit = { it.showToast() },
    viewModel: MultiSelectViewModel = koinViewModel(),
) {
    val navController = LocalNavController.current
    val context = LocalContext.current
    val windowSizeClass = LocalWindowSizeClass.current
    val currentSelectedStickers by rememberUpdatedState(selectedStickers)
    var openMultiStickersExportPathDialog by rememberSaveable { mutableStateOf(false) }
    var openDeleteMultiStickersDialog by rememberSaveable { mutableStateOf(false) }
    val dispatch = viewModel.getDispatcher(startWith = MultiSelectIntent.Init)
    val uiState by viewModel.viewState.collectAsStateWithLifecycle()

    val items = remember {
        arrayOf<@Composable () -> Unit>(
            @Composable {
                ComponeIconButton(
                    onClick = {
                        dispatch(MultiSelectIntent.SendStickers(currentSelectedStickers))
                        onRemoveSelectedStickers(currentSelectedStickers)
                    },
                    enabled = currentSelectedStickers.isNotEmpty(),
                    imageVector = Icons.Outlined.Share,
                    contentDescription = stringResource(id = R.string.send_sticker)
                )
            },
            @Composable {
                ComponeIconButton(
                    onClick = {
                        navController.navigate(
                            AddRoute(
                                stickers = currentSelectedStickers.map {
                                    UriWithStickerUuidBean(
                                        uri = stickerUuidToUri(it),
                                        stickerUuid = it,
                                    )
                                },
                                isEdit = true,
                            )
                        )
                        onRemoveSelectedStickers(currentSelectedStickers)
                    },
                    enabled = currentSelectedStickers.isNotEmpty(),
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = stringResource(id = R.string.add_screen_name_edit)
                )
            },
            @Composable {
                ComponeIconButton(
                    onClick = { openMultiStickersExportPathDialog = true },
                    enabled = currentSelectedStickers.isNotEmpty(),
                    imageVector = Icons.Outlined.Save,
                    contentDescription = stringResource(id = R.string.home_screen_export)
                )
            },
            @Composable {
                ComponeIconButton(
                    onClick = {
                        navController.navigate(ExportFilesRoute(exportStickers = currentSelectedStickers.toList()))
                        onRemoveSelectedStickers(currentSelectedStickers)
                    },
                    enabled = currentSelectedStickers.isNotEmpty(),
                    imageVector = Icons.Outlined.FolderZip,
                    contentDescription = stringResource(id = R.string.home_screen_export_to_backup_zip)
                )
            },
            @Composable {
                ComponeIconButton(
                    onClick = { openDeleteMultiStickersDialog = true },
                    enabled = currentSelectedStickers.isNotEmpty(),
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = stringResource(id = R.string.home_screen_delete)
                )
            },
            @Composable {
                ComponeIconButton(
                    onClick = {
                        currentSelectedStickers.firstOrNull()?.let {
                            navController.navigate(ImageSearchRoute(baseImage = stickerUuidToUri(it)))
                        }
                        onRemoveSelectedStickers(currentSelectedStickers)
                    },
                    enabled = currentSelectedStickers.size == 1,
                    imageVector = Icons.Outlined.ImageSearch,
                    contentDescription = stringResource(id = R.string.home_screen_image_search)
                )
            },
            @Composable {
                ComponeIconButton(
                    onClick = {
                        navController.navigate(MergeStickersRoute(stickerUuids = currentSelectedStickers.toList()))
                        onRemoveSelectedStickers(currentSelectedStickers)
                    },
                    enabled = currentSelectedStickers.size > 1,
                    imageVector = Icons.Outlined.Merge,
                    contentDescription = stringResource(id = R.string.merge_stickers_screen_merge)
                )
            },
        )
    }
    if (windowSizeClass.isCompact) {
        Row(
            modifier = modifier
                .horizontalScroll(rememberScrollState())
                .padding(contentPadding)
        ) {
            items.forEachIndexed { _, function -> function() }
        }
    } else {
        Column(
            modifier = modifier
                .verticalScroll(rememberScrollState())
                .padding(contentPadding)
        ) {
            items.forEachIndexed { _, function -> function() }
        }
    }

    MviEventListener(viewModel.singleEvent) { event ->
        when (event) {
            is MultiSelectEvent.DeleteStickerWithTags.Failed -> onMessage(event.msg)
            is MultiSelectEvent.ExportStickers.Success -> onMessage(
                context.resources.getQuantityString(
                    R.plurals.export_stickers_result,
                    event.successCount,
                    event.successCount,
                ),
            )
        }
    }

    ExportDialog(
        visible = openMultiStickersExportPathDialog,
        onDismissRequest = { openMultiStickersExportPathDialog = false },
        onExport = {
            dispatch(MultiSelectIntent.ExportStickers(selectedStickers))
            onRemoveSelectedStickers(selectedStickers)
        },
    )

    DeleteWarningDialog(
        visible = openDeleteMultiStickersDialog,
        onDismissRequest = { openDeleteMultiStickersDialog = false },
        onDismiss = { openDeleteMultiStickersDialog = false },
        onConfirm = {
            dispatch(MultiSelectIntent.DeleteStickerWithTags(selectedStickers))
            onRemoveSelectedStickers(selectedStickers)
            openDeleteMultiStickersDialog = false
        }
    )

    WaitingDialog(visible = uiState.loadingDialog)
}