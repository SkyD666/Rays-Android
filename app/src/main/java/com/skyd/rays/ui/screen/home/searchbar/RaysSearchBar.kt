package com.skyd.rays.ui.screen.home.searchbar

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.RichTooltip
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skyd.rays.R
import com.skyd.rays.config.refreshStickerData
import com.skyd.rays.ext.isCompact
import com.skyd.rays.ext.plus
import com.skyd.rays.model.bean.StickerWithTags
import com.skyd.rays.model.preference.CurrentStickerUuidPreference
import com.skyd.rays.model.preference.ExportStickerDirPreference
import com.skyd.rays.model.preference.search.QueryPreference
import com.skyd.rays.ui.component.RaysIconButton
import com.skyd.rays.ui.component.dialog.DeleteWarningDialog
import com.skyd.rays.ui.component.dialog.RaysDialog
import com.skyd.rays.ui.local.LocalCurrentStickerUuid
import com.skyd.rays.ui.local.LocalExportStickerDir
import com.skyd.rays.ui.local.LocalShowPopularTags
import com.skyd.rays.ui.local.LocalWindowSizeClass
import com.skyd.rays.ui.screen.home.HomeIntent
import com.skyd.rays.ui.screen.home.HomeState
import com.skyd.rays.ui.screen.home.HomeViewModel
import com.skyd.rays.ui.screen.home.PopularTagsUiState
import com.skyd.rays.ui.screen.home.SearchResultUiState
import com.skyd.rays.ui.screen.home.StickerDetailInfo
import com.skyd.rays.ui.screen.home.StickerDetailUiState


@Composable
fun RaysSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    active: Boolean,
    onActiveChange: (Boolean) -> Unit = {},
    stickerWithTags: StickerWithTags?,
    uiState: HomeState,
    viewModel: HomeViewModel = hiltViewModel()
) {
    var menuExpanded by rememberSaveable { mutableStateOf(false) }
    var multiSelect by rememberSaveable { mutableStateOf(false) }
    val selectedStickers = remember { mutableStateListOf<StickerWithTags>() }
    val windowSizeClass = LocalWindowSizeClass.current
    val currentStickerUuid = LocalCurrentStickerUuid.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val searchBarHorizontalPadding: Dp by animateDpAsState(
        targetValue = if (active) 0.dp else 16.dp,
        label = "searchBarHorizontalPadding"
    )
    val searchResultListState = rememberLazyStaggeredGridState()
    val showPopularTags = LocalShowPopularTags.current
    val popularTags =
        (uiState.popularTagsUiState as? PopularTagsUiState.Success)?.popularTags.orEmpty()
    var openDeleteWarningDialog by rememberSaveable { mutableStateOf(false) }
    var openExportPathDialog by rememberSaveable { mutableStateOf(false) }
    var openStickerInfoDialog by rememberSaveable { mutableStateOf(false) }
    var openDeleteMultiStickersDialog by rememberSaveable {
        mutableStateOf<Set<StickerWithTags>?>(null)
    }

    refreshStickerData.collectAsStateWithLifecycle(initialValue = null).apply {
        value ?: return@apply
        if (showPopularTags && active) {
            viewModel.sendUiIntent(HomeIntent.GetPopularTagsList)
        }
    }

    LaunchedEffect(showPopularTags) {
        if (showPopularTags && active) {
            viewModel.sendUiIntent(HomeIntent.GetPopularTagsList)
        }
    }

    Box(
        Modifier
            .semantics { isTraversalGroup = true }
            .fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(horizontal = searchBarHorizontalPadding)
        ) {
            SearchBar(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .semantics { traversalIndex = -1f },
                onQueryChange = onQueryChange,
                query = query,
                onSearch = { keyword ->
                    keyboardController?.hide()
                    onQueryChange(keyword)
                    viewModel.sendUiIntent(HomeIntent.GetStickerWithTagsList(keyword))
                },
                active = active,
                onActiveChange = {
                    if (it) {
                        if (showPopularTags) {
                            viewModel.sendUiIntent(HomeIntent.GetPopularTagsList)
                        }
                    } else {
                        onQueryChange(query)
                    }
                    onActiveChange(it)
                },
                placeholder = { Text(text = stringResource(R.string.home_screen_search_hint)) },
                leadingIcon = {
                    if (active) {
                        RaysIconButton(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = stringResource(id = R.string.home_screen_close_search),
                            onClick = { onActiveChange(false) }
                        )
                    } else {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null)
                    }
                },
                trailingIcon = {
                    if (active) {
                        if (query.isNotEmpty()) {
                            TrailingIcon(showClearButton = query.isNotEmpty()) {
                                onQueryChange(QueryPreference.default)
                            }
                        }
                    } else {
                        RaysIconButton(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = stringResource(id = R.string.home_screen_open_menu),
                            onClick = { menuExpanded = true }
                        )
                        HomeMenu(
                            expanded = menuExpanded,
                            stickerMenuItemEnabled = uiState.stickerDetailUiState is StickerDetailUiState.Success,
                            onDismissRequest = { menuExpanded = false },
                            onDeleteClick = { openDeleteWarningDialog = true },
                            onExportClick = { openExportPathDialog = true },
                            onStickerInfoClick = { openStickerInfoDialog = true },
                            onClearScreen = {
                                viewModel.sendUiIntent(
                                    HomeIntent.GetStickerDetails(CurrentStickerUuidPreference.default)
                                )
                            }
                        )
                    }
                },
            ) {
                AnimatedVisibility(visible = LocalShowPopularTags.current && popularTags.isNotEmpty()) {
                    PopularTagsBar(
                        onTagClicked = { onQueryChange(query + it) },
                        tags = popularTags,
                    )
                }
                val searchResultUiState = uiState.searchResultUiState
                if (searchResultUiState is SearchResultUiState.Success) {
                    val searchResultList = @Composable {
                        if (!active) multiSelect = false
                        SearchResultList(
                            state = searchResultListState,
                            dataList = searchResultUiState.stickerWithTagsList,
                            onItemClickListener = { data, selected ->
                                if (multiSelect) {
                                    if (selected) selectedStickers.add(data)
                                    else selectedStickers.remove(data)
                                } else {
                                    onActiveChange(false)
                                    viewModel.sendUiIntent(
                                        HomeIntent.AddClickCountAndGetStickerDetails(
                                            stickerUuid = data.sticker.uuid,
                                        )
                                    )
                                }
                            },
                            multiSelect = multiSelect,
                            onMultiSelectChanged = {
                                multiSelect = it
                                if (!it) {
                                    selectedStickers.clear()
                                }
                            },
                            onInvertSelectClick = {
                                val newSelectedStickers =
                                    searchResultUiState.stickerWithTagsList - selectedStickers
                                selectedStickers.clear()
                                selectedStickers.addAll(newSelectedStickers)
                            },
                            selectedStickers = selectedStickers,
                        )
                    }
                    val multiSelectBar: @Composable (compact: Boolean) -> Unit =
                        @Composable { compact ->
                            AnimatedVisibility(
                                visible = multiSelect,
                                enter = if (compact) expandVertically() else expandHorizontally(),
                                exit = if (compact) shrinkVertically() else shrinkHorizontally(),
                            ) {
                                var openMultiStickersExportPathDialog by rememberSaveable {
                                    mutableStateOf(false)
                                }
                                MultiSelectActionBar(
                                    selectedStickers = selectedStickers,
                                    onDeleteClick = {
                                        openDeleteMultiStickersDialog =
                                            searchResultUiState.stickerWithTagsList.toSet()
                                    },
                                    onExportClick = { openMultiStickersExportPathDialog = true },
                                )
                                ExportDialog(
                                    visible = openMultiStickersExportPathDialog,
                                    onDismissRequest = {
                                        openMultiStickersExportPathDialog = false
                                    },
                                    onExport = {
                                        val uuidList = selectedStickers.map { it.sticker.uuid }
                                        viewModel.sendUiIntent(HomeIntent.ExportStickers(uuidList))
                                    },
                                )
                            }
                        }
                    if (windowSizeClass.isCompact) {
                        Box(modifier = Modifier.weight(1f)) { searchResultList() }
                        multiSelectBar(true)
                    } else {
                        Row {
                            multiSelectBar(false)
                            searchResultList()
                        }
                    }
                }
            }
        }

        RaysDialog(
            visible = openStickerInfoDialog && stickerWithTags != null,
            title = { Text(text = stringResource(id = R.string.home_screen_sticker_info)) },
            text = {
                StickerDetailInfo(stickerWithTags = stickerWithTags!!)
            },
            confirmButton = {
                TextButton(onClick = { openStickerInfoDialog = false }) {
                    Text(text = stringResource(id = R.string.dialog_ok))
                }
            },
            onDismissRequest = { openStickerInfoDialog = false }
        )

        if (currentStickerUuid.isNotBlank()) {
            DeleteWarningDialog(
                visible = openDeleteWarningDialog,
                onDismissRequest = { openDeleteWarningDialog = false },
                onDismiss = { openDeleteWarningDialog = false },
                onConfirm = {
                    openDeleteWarningDialog = false
                    viewModel.sendUiIntent(
                        HomeIntent.DeleteStickerWithTags(
                            listOf(currentStickerUuid)
                        )
                    )
                }
            )
        }

        // 删除多选的表情包警告
        DeleteWarningDialog(
            visible = openDeleteMultiStickersDialog != null,
            onDismissRequest = { openDeleteMultiStickersDialog = null },
            onDismiss = { openDeleteMultiStickersDialog = null },
            onConfirm = {
                viewModel.sendUiIntent(
                    HomeIntent.DeleteStickerWithTags(
                        selectedStickers.map { it.sticker.uuid }
                    )
                )
                // 去除所有被删除了，但还在selectedStickers中的数据
                selectedStickers -= openDeleteMultiStickersDialog!!
                openDeleteMultiStickersDialog = null
            }
        )

        ExportDialog(
            visible = openExportPathDialog,
            onDismissRequest = { openExportPathDialog = false },
            onExport = { viewModel.sendUiIntent(HomeIntent.ExportStickers(listOf(currentStickerUuid))) },
        )
    }
}

@Composable
internal fun ExportDialog(
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
                        pickExportDirLauncher.launch(Uri.parse(exportStickerDir))
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

@Composable
fun TrailingIcon(
    showClearButton: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    if (showClearButton) {
        RaysIconButton(
            imageVector = Icons.Default.Clear,
            contentDescription = stringResource(R.string.home_screen_clear_search_text),
            onClick = { onClick?.invoke() }
        )
    }
}

@Composable
fun PopularTagsBar(
    onTagClicked: (String) -> Unit,
    tags: List<Pair<String, Float>>,
) {
    val eachTag: @Composable (Pair<String, Float>) -> Unit = { item ->
        TooltipBox(
            positionProvider = TooltipDefaults.rememberRichTooltipPositionProvider(),
            tooltip = {
                RichTooltip(
                    title = { Text(item.first) },
                    text = {
                        Text(
                            text = stringResource(
                                R.string.home_screen_popular_tags_popular_value, item.second
                            )
                        )
                    }
                )
            },
            state = rememberTooltipState(),
        ) {
            Text(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { onTagClicked(item.first) }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                text = item.first
            )
        }
    }

    Box {
        Row {
            var expand by rememberSaveable { mutableStateOf(false) }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically)
            ) {
                val lazyRowState = rememberLazyListState()
                val flowRowState = rememberScrollState()
                androidx.compose.animation.AnimatedVisibility(
                    visible = !expand,
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    LazyRow(
                        state = lazyRowState,
                        contentPadding = PaddingValues(start = 16.dp) + PaddingValues(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        itemsIndexed(tags) { index, item ->
                            eachTag(item)
                            if (index < tags.size - 1) {
                                VerticalDivider(modifier = Modifier.height(16.dp))
                            }
                        }
                    }
                }
                androidx.compose.animation.AnimatedVisibility(
                    visible = expand,
                    enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
                    exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut(),
                ) {
                    FlowRow(
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .heightIn(max = 200.dp)
                            .verticalScroll(flowRowState)
                            .padding(vertical = 6.dp),
                    ) {
                        tags.forEachIndexed { index, item ->
                            eachTag(item)
                            if (index < tags.size - 1) {
                                VerticalDivider(
                                    modifier = Modifier
                                        .height(16.dp)
                                        .align(Alignment.CenterVertically)
                                )
                            }
                        }
                    }
                }
            }

            RaysIconButton(
                imageVector = if (expand) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (expand) stringResource(R.string.collapse)
                else stringResource(R.string.expand),
                onClick = { expand = !expand },
            )
        }

        HorizontalDivider(
            Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = 16.dp)
        )
    }
}
