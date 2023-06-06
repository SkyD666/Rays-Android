package com.skyd.rays.ui.screen.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ManageSearch
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.isContainer
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skyd.rays.R
import com.skyd.rays.base.LoadUiIntent
import com.skyd.rays.config.refreshStickerData
import com.skyd.rays.ext.dateTime
import com.skyd.rays.ext.plus
import com.skyd.rays.ext.screenIsLand
import com.skyd.rays.model.bean.StickerWithTags
import com.skyd.rays.model.preference.CurrentStickerUuidPreference
import com.skyd.rays.model.preference.StickerScalePreference
import com.skyd.rays.model.preference.search.QueryPreference
import com.skyd.rays.model.preference.search.SearchResultReversePreference
import com.skyd.rays.model.preference.search.SearchResultSortPreference
import com.skyd.rays.ui.component.AnimatedPlaceholder
import com.skyd.rays.ui.component.RaysFloatingActionButton
import com.skyd.rays.ui.component.RaysIconButton
import com.skyd.rays.ui.component.RaysIconButtonStyle
import com.skyd.rays.ui.component.RaysImage
import com.skyd.rays.ui.component.RaysOutlinedCard
import com.skyd.rays.ui.component.dialog.DeleteWarningDialog
import com.skyd.rays.ui.component.dialog.RaysDialog
import com.skyd.rays.ui.local.LocalCurrentStickerUuid
import com.skyd.rays.ui.local.LocalHomeShareButtonAlignment
import com.skyd.rays.ui.local.LocalNavController
import com.skyd.rays.ui.local.LocalQuery
import com.skyd.rays.ui.local.LocalSearchResultReverse
import com.skyd.rays.ui.local.LocalSearchResultSort
import com.skyd.rays.ui.local.LocalStickerScale
import com.skyd.rays.ui.local.LocalWindowSizeClass
import com.skyd.rays.ui.screen.add.ADD_SCREEN_ROUTE
import com.skyd.rays.ui.screen.settings.searchconfig.SEARCH_CONFIG_SCREEN_ROUTE
import com.skyd.rays.util.sendSticker
import kotlinx.coroutines.launch

private var openDeleteWarningDialog by mutableStateOf(false)

@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val currentStickerUuid = LocalCurrentStickerUuid.current
    val initQuery = LocalQuery.current
    var query by rememberSaveable(initQuery) { mutableStateOf(initQuery) }
    val uiState by viewModel.uiStateFlow.collectAsStateWithLifecycle()
    val loadUiIntent by viewModel.loadUiIntentFlow.collectAsStateWithLifecycle(initialValue = null)

    refreshStickerData.collectAsStateWithLifecycle(initialValue = null).apply {
        value ?: return@apply
        viewModel.sendUiIntent(HomeIntent.GetStickerWithTagsList(query))
        viewModel.sendUiIntent(HomeIntent.GetStickerDetails(currentStickerUuid))
    }

    LaunchedEffect(query) {
        viewModel.sendUiIntent(HomeIntent.GetStickerWithTagsList(query))
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        contentWindowInsets = if (context.screenIsLand) {
            WindowInsets(
                left = 0,
                top = 0,
                right = ScaffoldDefaults.contentWindowInsets
                    .getRight(LocalDensity.current, LocalLayoutDirection.current),
                bottom = 0
            )
        } else {
            WindowInsets(0.dp)
        }
    ) { innerPaddings ->
        Column(
            modifier = Modifier
                .padding(innerPaddings)
                .fillMaxSize()
        ) {
            uiState.apply {
                when (stickerDetailUiState) {
                    is StickerDetailUiState.Init -> {
                        RaysSearchBar(
                            query = query,
                            onQueryChange = { query = it },
                            stickerWithTags = null,
                            uiState = uiState,
                        )
                        AnimatedPlaceholder(
                            resId = R.raw.lottie_genshin_impact_venti_1,
                            tip = stringResource(id = R.string.home_screen_empty_tip)
                        )
                        if (stickerDetailUiState.stickerUuid.isNotBlank()) {
                            viewModel.sendUiIntent(
                                HomeIntent.GetStickerDetails(stickerDetailUiState.stickerUuid)
                            )
                        }
                    }

                    is StickerDetailUiState.Success -> {
                        val stickerWithTags = stickerDetailUiState.stickerWithTags
                        RaysSearchBar(
                            query = query,
                            onQueryChange = { query = it },
                            stickerWithTags = stickerWithTags,
                            uiState = uiState,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        MainCard(stickerWithTags = stickerWithTags)
                    }
                }
            }
        }

        loadUiIntent?.also { loadUiIntent ->
            when (loadUiIntent) {
                is LoadUiIntent.Error -> {
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = context.getString(
                                R.string.home_screen_failed, loadUiIntent.msg
                            ),
                            withDismissAction = true
                        )
                    }
                }

                is LoadUiIntent.Loading -> Unit
            }
        }

        if (openDeleteWarningDialog && currentStickerUuid.isNotBlank()) {
            DeleteWarningDialog(
                visible = openDeleteWarningDialog,
                onDismissRequest = { openDeleteWarningDialog = false },
                onDismiss = { openDeleteWarningDialog = false },
                onConfirm = {
                    openDeleteWarningDialog = false
                    viewModel.sendUiIntent(HomeIntent.DeleteStickerWithTags(currentStickerUuid))
                }
            )
        }
    }
}

@Composable
private fun RaysSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    stickerWithTags: StickerWithTags?,
    uiState: HomeState,
    viewModel: HomeViewModel = hiltViewModel()
) {
    var menuExpanded by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    val navController = LocalNavController.current
    val scope = rememberCoroutineScope()
    var active by rememberSaveable { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val searchBarHorizontalPadding: Dp by animateDpAsState(if (active) 0.dp else 16.dp)
    val searchResultListState = rememberLazyStaggeredGridState()
    var openStickerInfoDialog by rememberSaveable { mutableStateOf(false) }

    Box(
        Modifier
            .semantics { isContainer = true }
            .zIndex(1f)
            .fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(horizontal = searchBarHorizontalPadding)
        ) {
            SearchBar(
                onQueryChange = onQueryChange,
                query = query,
                onSearch = { keyword ->
                    keyboardController?.hide()
                    QueryPreference.put(context, scope, keyword)
                    viewModel.sendUiIntent(HomeIntent.GetStickerWithTagsList(keyword))
                },
                active = active,
                onActiveChange = {
                    if (!it) {
                        QueryPreference.put(context, scope, query)
                    }
                    active = it
                },
                placeholder = { Text(text = stringResource(R.string.home_screen_search_hint)) },
                leadingIcon = {
                    if (active) {
                        RaysIconButton(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(id = R.string.home_screen_close_search),
                            onClick = { active = false }
                        )
                    } else {
                        RaysIconButton(
                            imageVector = Icons.Default.Menu,
                            contentDescription = stringResource(id = R.string.home_screen_open_menu),
                            onClick = { menuExpanded = true }
                        )
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
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(R.string.home_screen_add),
                            onClick = { navController.navigate(ADD_SCREEN_ROUTE) }
                        )
                    }
                },
            ) {
                val searchResultUiState = uiState.searchResultUiState
                if (searchResultUiState is SearchResultUiState.Success) {
                    SearchResultList(
                        state = searchResultListState,
                        dataList = searchResultUiState.stickerWithTagsList,
                        onItemClickListener = {
                            active = false
                            viewModel.sendUiIntent(
                                HomeIntent.AddClickCountAndGetStickerDetails(stickerUuid = it.sticker.uuid)
                            )
                        }
                    )
                }
            }
            HomeMenu(
                expanded = menuExpanded,
                stickerMenuItemEnabled = uiState.stickerDetailUiState is StickerDetailUiState.Success,
                onDismissRequest = { menuExpanded = false },
                onStickerInfoClick = { openStickerInfoDialog = true }
            )
        }

        RaysDialog(
            visible = openStickerInfoDialog && stickerWithTags != null,
            title = { Text(text = stringResource(id = R.string.home_screen_sticker_info)) },
            text = {
                val sticker = stickerWithTags!!.sticker
                val createTime = dateTime(sticker.createTime)
                Text(
                    text = stringResource(
                        id = R.string.home_screen_sticker_info_desc,
                        sticker.uuid,
                        sticker.stickerMd5,
                        sticker.clickCount,
                        sticker.shareCount,
                        createTime,
                        stickerWithTags.sticker.modifyTime?.let { dateTime(it) } ?: createTime,
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = { openStickerInfoDialog = false }) {
                    Text(text = stringResource(id = R.string.dialog_ok))
                }
            },
            onDismissRequest = { openStickerInfoDialog = false }
        )
    }
}

@Composable
fun SearchResultConfigBar(size: Int) {
    val searchResultReverse = LocalSearchResultReverse.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var expandMenu by rememberSaveable { mutableStateOf(false) }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Badge(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 16.dp),
        ) {
            AnimatedContent(targetState = size) { targetCount ->
                Text(text = targetCount.toString())
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        FilterChip(
            modifier = Modifier.padding(horizontal = 6.dp),
            selected = searchResultReverse,
            onClick = {
                SearchResultReversePreference.put(
                    context = context,
                    scope = scope,
                    value = !searchResultReverse
                )
            },
            label = { Text(text = stringResource(R.string.search_result_reverse)) },
            leadingIcon = {
                if (searchResultReverse) {
                    Icon(
                        imageVector = Icons.Default.Done,
                        contentDescription = null,
                        modifier = Modifier.size(FilterChipDefaults.IconSize),
                    )
                }
            }
        )

        Box(modifier = Modifier.padding(end = 16.dp)) {
            FilterChip(
                selected = false,
                onClick = { expandMenu = !expandMenu },
                label = { Text(text = stringResource(R.string.search_result_sort)) },
                trailingIcon = {
                    Icon(
                        imageVector = if (expandMenu) Icons.Default.ArrowDropUp
                        else Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        modifier = Modifier.size(AssistChipDefaults.IconSize),
                    )
                }
            )
            SearchResultSortMenu(expanded = expandMenu, onDismissRequest = { expandMenu = false })
        }
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
fun SearchResultList(
    state: LazyStaggeredGridState,
    dataList: List<StickerWithTags>,
    onItemClickListener: ((data: StickerWithTags) -> Unit)? = null,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val searchResultSort = LocalSearchResultSort.current
    val searchResultReverse = LocalSearchResultReverse.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(searchResultSort) {
        viewModel.sendUiIntent(HomeIntent.SortStickerWithTagsList(dataList))
    }
    LaunchedEffect(searchResultReverse) {
        viewModel.sendUiIntent(HomeIntent.ReverseStickerWithTagsList(dataList))
    }

    Column {
        SearchResultConfigBar(size = dataList.size)
        if (dataList.isEmpty()) {
            AnimatedPlaceholder(
                resId = R.raw.lottie_genshin_impact_klee_2,
                tip = stringResource(id = R.string.home_screen_no_search_result_tip)
            )
        } else {
            Scaffold(
                floatingActionButton = {
                    RaysFloatingActionButton(
                        onClick = { scope.launch { state.animateScrollToItem(0) } },
                        contentDescription = stringResource(R.string.home_screen_search_result_list_to_top),
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = null
                        )
                    }
                },
                contentWindowInsets = WindowInsets(0.dp),
            ) { paddingValues ->
                val windowSizeClass = LocalWindowSizeClass.current
                LazyVerticalStaggeredGrid(
                    modifier = Modifier.fillMaxSize(),
                    state = state,
                    contentPadding = paddingValues +
                            PaddingValues(horizontal = 16.dp) +
                            PaddingValues(bottom = 16.dp),
                    columns = StaggeredGridCells.Fixed(
                        if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact) 2 else 4
                    ),
                    verticalItemSpacing = 12.dp,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(items = dataList, key = { it.sticker.uuid }) {
                        SearchResultItem(data = it, onClickListener = onItemClickListener)
                    }
                }
            }
        }
    }
}

@Composable
fun SearchResultItem(
    modifier: Modifier = Modifier,
    data: StickerWithTags,
    onClickListener: ((data: StickerWithTags) -> Unit)? = null
) {
    val context = LocalContext.current
    RaysOutlinedCard(
        modifier = modifier.fillMaxWidth(),
        onLongClick = {
            context.sendSticker(
                uuid = data.sticker.uuid,
                onSuccess = { data.sticker.shareCount++ }
            )
        },
        onClick = {
            onClickListener?.invoke(data)
        }
    ) {
        RaysImage(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            contentScale = ContentScale.Crop,
            uuid = data.sticker.uuid
        )
        if (data.sticker.title.isNotBlank()) {
            Text(
                modifier = Modifier
                    .padding(vertical = 10.dp, horizontal = 10.dp)
                    .basicMarquee(iterations = Int.MAX_VALUE),
                text = data.sticker.title,
                style = MaterialTheme.typography.titleMedium,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun SearchResultSortMenu(
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
private fun HomeMenu(
    expanded: Boolean,
    stickerMenuItemEnabled: Boolean,
    onDismissRequest: () -> Unit,
    onStickerInfoClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
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
                viewModel.sendUiIntent(
                    HomeIntent.GetStickerDetails(CurrentStickerUuidPreference.default)
                )
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
                navController.navigate("$ADD_SCREEN_ROUTE?stickerUuid=${currentStickerUuid}")
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
                openDeleteWarningDialog = true
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
        Divider()
        DropdownMenuItem(
            text = { Text(stringResource(R.string.search_config_screen_name)) },
            onClick = {
                navController.navigate(SEARCH_CONFIG_SCREEN_ROUTE)
                onDismissRequest()
            },
            leadingIcon = {
                Icon(
                    Icons.Default.ManageSearch,
                    contentDescription = null
                )
            }
        )
    }
}

@Composable
private fun MainCard(stickerWithTags: StickerWithTags) {
    val navController = LocalNavController.current
    val currentStickerUuid = LocalCurrentStickerUuid.current
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    val stickerBean = stickerWithTags.sticker
    val tags = stickerWithTags.tags

    Card(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp)
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .combinedClickable(
                    onLongClick = {
                        context.sendSticker(
                            uuid = stickerBean.uuid,
                            onSuccess = { stickerBean.shareCount++ }
                        )
                    },
                    onDoubleClick = {
                        navController.navigate("$ADD_SCREEN_ROUTE?stickerUuid=${currentStickerUuid}")
                    },
                    onClick = {}
                )
        ) {
            Box {
                RaysImage(
                    modifier = Modifier.fillMaxWidth(),
                    uuid = stickerBean.uuid,
                    contentScale = StickerScalePreference.toContentScale(LocalStickerScale.current),
                )
                Box(
                    modifier = Modifier.matchParentSize(),
                    contentAlignment = LocalHomeShareButtonAlignment.current
                ) {
                    RaysIconButton(
                        style = RaysIconButtonStyle.FilledTonal,
                        imageVector = Icons.Default.Share,
                        contentDescription = stringResource(R.string.home_screen_send_sticker),
                        onClick = {
                            context.sendSticker(
                                uuid = stickerBean.uuid,
                                onSuccess = { stickerBean.shareCount++ }
                            )
                        }
                    )
                }
            }
            if (stickerBean.title.isNotBlank()) {
                Text(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(top = 16.dp, bottom = if (tags.isEmpty()) 16.dp else 0.dp)
                        .basicMarquee(iterations = Int.MAX_VALUE),
                    text = stickerBean.title,
                    style = MaterialTheme.typography.titleLarge
                )
            }
            if (tags.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier
                        .padding(vertical = 6.dp, horizontal = 16.dp)
                        .fillMaxWidth()
                        .heightIn(max = 150.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    repeat(tags.size) { index ->
                        AssistChip(
                            onClick = { clipboardManager.setText(AnnotatedString(tags[index].tag)) },
                            label = { Text(tags[index].tag) }
                        )
                    }
                }
            }
        }
    }
}
