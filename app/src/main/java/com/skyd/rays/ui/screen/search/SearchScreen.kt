package com.skyd.rays.ui.screen.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RichTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skyd.rays.R
import com.skyd.rays.base.mvi.getDispatcher
import com.skyd.rays.ext.isCompact
import com.skyd.rays.ext.plus
import com.skyd.rays.ext.showSnackbarWithLaunchedEffect
import com.skyd.rays.model.bean.StickerWithTags
import com.skyd.rays.model.bean.UriWithStickerUuidBean
import com.skyd.rays.model.preference.search.QueryPreference
import com.skyd.rays.ui.component.BackIcon
import com.skyd.rays.ui.component.RaysFloatingActionButton
import com.skyd.rays.ui.component.RaysIconButton
import com.skyd.rays.ui.component.dialog.DeleteWarningDialog
import com.skyd.rays.ui.component.dialog.ExportDialog
import com.skyd.rays.ui.local.LocalNavController
import com.skyd.rays.ui.local.LocalQuery
import com.skyd.rays.ui.local.LocalShowPopularTags
import com.skyd.rays.ui.local.LocalWindowSizeClass
import com.skyd.rays.ui.screen.add.openAddScreen
import com.skyd.rays.ui.screen.detail.openDetailScreen
import com.skyd.rays.ui.screen.settings.data.importexport.file.exportfiles.openExportFilesScreen
import com.skyd.rays.util.stickerUuidToUri
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

const val SEARCH_SCREEN_ROUTE = "searchScreen"

@Composable
fun SearchScreen(viewModel: SearchViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val uiState by viewModel.viewState.collectAsStateWithLifecycle()
    val uiEvent by viewModel.singleEvent.collectAsStateWithLifecycle(initialValue = null)
    val navController = LocalNavController.current
    var multiSelect by rememberSaveable { mutableStateOf(false) }
    val selectedStickers = remember { mutableStateListOf<StickerWithTags>() }
    val windowSizeClass = LocalWindowSizeClass.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val searchResultListState = rememberLazyStaggeredGridState()
    val popularTags =
        (uiState.searchDataState as? SearchDataState.Success)?.popularTags.orEmpty()
    var openDeleteMultiStickersDialog by rememberSaveable {
        mutableStateOf<Set<StickerWithTags>?>(null)
    }
    val query = LocalQuery.current
    var fabHeight by remember { mutableStateOf(0.dp) }
    var fabWidht by remember { mutableStateOf(0.dp) }
    var searchFieldValueState by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(text = query, selection = TextRange(query.length)))
    }

    val dispatch = viewModel.getDispatcher(startWith = SearchIntent.GetSearchData)

    Scaffold(
        modifier = Modifier.imePadding(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            AnimatedVisibility(visible = searchResultListState.firstVisibleItemIndex > 2) {
                RaysFloatingActionButton(
                    onClick = { scope.launch { searchResultListState.animateScrollToItem(0) } },
                    onSizeWithSinglePaddingChanged = { width, height ->
                        fabWidht = width
                        fabHeight = height
                    },
                    contentDescription = stringResource(R.string.home_screen_search_result_list_to_top),
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = null
                    )
                }
            }
        },
        topBar = {
            LaunchedEffect(searchFieldValueState.text) {
                delay(60)
                QueryPreference.put(context, scope, searchFieldValueState.text)
            }
            SearchBarInputField(
                modifier = Modifier.windowInsetsPadding(
                    WindowInsets.systemBars
                        .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
                ),
                onQueryChange = { searchFieldValueState = it },
                query = searchFieldValueState,
                onSearch = { state ->
                    keyboardController?.hide()
                    searchFieldValueState = state
                },
                placeholder = { Text(text = stringResource(R.string.home_screen_search_hint)) },
                leadingIcon = { BackIcon() },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        TrailingIcon(showClearButton = query.isNotEmpty()) {
                            searchFieldValueState = TextFieldValue(
                                text = QueryPreference.default,
                                selection = TextRange(QueryPreference.default.length)
                            )
                        }
                    }
                }
            )
        }
    ) { innerPaddings ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(innerPaddings),
        ) {
            HorizontalDivider()
            AnimatedVisibility(
                visible = LocalShowPopularTags.current && popularTags.isNotEmpty(),
                enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
            ) {
                PopularTagsBar(
                    onTagClicked = {
                        searchFieldValueState = TextFieldValue(
                            text = searchFieldValueState.text + it,
                            selection = TextRange((searchFieldValueState.text + it).length)
                        )
                    },
                    tags = popularTags,
                )
            }
            val searchResultUiState = uiState.searchDataState
            if (searchResultUiState is SearchDataState.Success) {
                val searchResultList = @Composable {
                    SearchResultList(
                        state = searchResultListState,
                        contentPadding = PaddingValues(bottom = fabHeight + 16.dp) +
                                PaddingValues(horizontal = 16.dp),
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
                                modifier = Modifier
                                    .run {
                                        if (windowSizeClass.isCompact) padding(end = fabWidht)
                                        else this
                                    }
                                    .horizontalScroll(rememberScrollState()),
                                selectedStickers = selectedStickers,
                                onEditClick = {
                                    openAddScreen(
                                        navController = navController,
                                        stickers = selectedStickers.map {
                                            UriWithStickerUuidBean(
                                                uri = stickerUuidToUri(it.sticker.uuid),
                                                stickerUuid = it.sticker.uuid,
                                            )
                                        },
                                        isEdit = true
                                    )
                                },
                                onDeleteClick = {
                                    openDeleteMultiStickersDialog =
                                        searchResultUiState.stickerWithTagsList.toSet()
                                },
                                onExportClick = { openMultiStickersExportPathDialog = true },
                                onExportAsZipClick = {
                                    openExportFilesScreen(
                                        navController = navController,
                                        exportStickers = selectedStickers.map { it.sticker.uuid },
                                    )
                                },
                            )
                            ExportDialog(
                                visible = openMultiStickersExportPathDialog,
                                onDismissRequest = {
                                    openMultiStickersExportPathDialog = false
                                },
                                onExport = {
                                    dispatch(SearchIntent.ExportStickers(
                                        selectedStickers.map { it.sticker.uuid }
                                    ))
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

    when (val event = uiEvent) {
        is SearchEvent.ExportStickers.Success -> snackbarHostState.showSnackbarWithLaunchedEffect(
            message = context.resources.getQuantityString(
                R.plurals.export_stickers_result,
                event.successCount,
                event.successCount,
            ),
            key2 = uiEvent,
        )

        is SearchEvent.DeleteStickerWithTags.Success,
        null -> Unit
    }

    // 删除多选的表情包警告
    DeleteWarningDialog(
        visible = openDeleteMultiStickersDialog != null,
        onDismissRequest = { openDeleteMultiStickersDialog = null },
        onDismiss = { openDeleteMultiStickersDialog = null },
        onConfirm = {
            dispatch(
                SearchIntent.DeleteStickerWithTags(selectedStickers.map { it.sticker.uuid })
            )
            // 去除所有被删除了，但还在selectedStickers中的数据
            selectedStickers -= openDeleteMultiStickersDialog!!
            openDeleteMultiStickersDialog = null
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

@Composable
private fun SearchBarInputField(
    query: TextFieldValue,
    onQueryChange: (TextFieldValue) -> Unit,
    onSearch: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    val focusRequester = remember { FocusRequester() }
    TextField(
        modifier = modifier
            .focusRequester(focusRequester)
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
            .height(72.dp),
        value = query,
        onValueChange = onQueryChange,
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            errorIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
        ),
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        placeholder = placeholder,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch(query) }),
        interactionSource = interactionSource,
        singleLine = true,
        shape = RectangleShape,
    )

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}