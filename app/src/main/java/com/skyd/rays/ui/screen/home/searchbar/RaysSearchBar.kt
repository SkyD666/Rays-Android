package com.skyd.rays.ui.screen.home.searchbar

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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.RichTooltip
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.skyd.rays.model.preference.search.QueryPreference
import com.skyd.rays.ui.component.RaysIconButton
import com.skyd.rays.ui.component.dialog.DeleteWarningDialog
import com.skyd.rays.ui.component.dialog.ExportDialog
import com.skyd.rays.ui.local.LocalNavController
import com.skyd.rays.ui.local.LocalShowPopularTags
import com.skyd.rays.ui.local.LocalWindowSizeClass
import com.skyd.rays.ui.screen.detail.openDetailScreen
import com.skyd.rays.ui.screen.home.HomeIntent
import com.skyd.rays.ui.screen.home.HomeState
import com.skyd.rays.ui.screen.home.HomeViewModel
import com.skyd.rays.ui.screen.home.PopularTagsUiState
import com.skyd.rays.ui.screen.home.SearchResultUiState


@Composable
fun RaysSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    active: Boolean,
    onActiveChange: (Boolean) -> Unit = {},
    uiState: HomeState,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val navController = LocalNavController.current
    var multiSelect by rememberSaveable { mutableStateOf(false) }
    val selectedStickers = remember { mutableStateListOf<StickerWithTags>() }
    val windowSizeClass = LocalWindowSizeClass.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val searchBarHorizontalPadding: Dp by animateDpAsState(
        targetValue = if (active) 0.dp else 16.dp,
        label = "searchBarHorizontalPadding"
    )
    val searchResultListState = rememberLazyStaggeredGridState()
    val showPopularTags = LocalShowPopularTags.current
    val popularTags =
        (uiState.popularTagsUiState as? PopularTagsUiState.Success)?.popularTags.orEmpty()
    var openDeleteMultiStickersDialog by rememberSaveable {
        mutableStateOf<Set<StickerWithTags>?>(null)
    }

    refreshStickerData.collectAsStateWithLifecycle(initialValue = null).apply {
        value ?: return@apply
        viewModel.sendUiIntent(HomeIntent.GetStickerWithTagsList(query))
        if (showPopularTags && active) {
            viewModel.sendUiIntent(HomeIntent.GetSearchBarPopularTagsList)
        }
    }

    LaunchedEffect(query) {
        viewModel.sendUiIntent(HomeIntent.GetStickerWithTagsList(query))
    }

    LaunchedEffect(showPopularTags) {
        if (showPopularTags && active) {
            viewModel.sendUiIntent(HomeIntent.GetSearchBarPopularTagsList)
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
                            viewModel.sendUiIntent(HomeIntent.GetSearchBarPopularTagsList)
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
                                    openDetailScreen(
                                        navController = navController,
                                        stickerUuid = data.sticker.uuid
                                    )
                                    viewModel.sendUiIntent(
                                        HomeIntent.AddClickCount(stickerUuid = data.sticker.uuid)
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
    }
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
