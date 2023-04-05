package com.skyd.rays.ui.screen.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.*
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
import com.skyd.rays.appContext
import com.skyd.rays.base.LoadUiIntent
import com.skyd.rays.config.refreshStickerData
import com.skyd.rays.ext.screenIsLand
import com.skyd.rays.model.bean.StickerWithTags
import com.skyd.rays.model.preference.QueryPreference
import com.skyd.rays.model.preference.rememberQuery
import com.skyd.rays.ui.component.*
import com.skyd.rays.ui.component.dialog.DeleteWarningDialog
import com.skyd.rays.ui.local.LocalCurrentStickerUuid
import com.skyd.rays.ui.local.LocalNavController
import com.skyd.rays.ui.screen.add.ADD_SCREEN_ROUTE
import com.skyd.rays.ui.screen.settings.searchconfig.SEARCH_CONFIG_SCREEN_ROUTE
import com.skyd.rays.util.sendSticker
import kotlinx.coroutines.launch

private var menuExpanded by mutableStateOf(false)
private var openDeleteWarningDialog by mutableStateOf(false)

@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val currentStickerUuid = LocalCurrentStickerUuid.current
    var query by rememberQuery()
    var stickerWithTags by remember { mutableStateOf<StickerWithTags?>(null) }

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
            RaysSearchBar(query = { query }, onQueryChange = { query = it })
            Spacer(modifier = Modifier.height(16.dp))

            AnimatedVisibility(
                visible = stickerWithTags != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                stickerWithTags?.let {
                    MainCard(stickerWithTags = it)
                }
            }

            viewModel.uiStateFlow.collectAsStateWithLifecycle().value.apply {
                when (stickerDetailUiState) {
                    is StickerDetailUiState.Init -> {
                        stickerWithTags = null
                        AnimatedPlaceholder(
                            resId = R.raw.lottie_genshin_impact_klee_1,
                            tip = stringResource(id = R.string.home_screen_empty_tip)
                        )
                        if (stickerDetailUiState.stickerUuid.isNotBlank()) {
                            viewModel.sendUiIntent(
                                HomeIntent.GetStickerDetails(stickerDetailUiState.stickerUuid)
                            )
                        }
                    }
                    is StickerDetailUiState.Success -> {
                        stickerWithTags = stickerDetailUiState.stickerWithTags
                    }
                }
            }
        }

        viewModel.loadUiIntentFlow.collectAsStateWithLifecycle(initialValue = null).value?.also { loadUiIntent ->
            when (loadUiIntent) {
                is LoadUiIntent.Error -> {
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = appContext.getString(
                                R.string.home_screen_failed, loadUiIntent.msg
                            ),
                            withDismissAction = true
                        )
                    }
                }
                is LoadUiIntent.Loading -> {}
                LoadUiIntent.ShowMainView -> {}
            }
        }

        if (openDeleteWarningDialog && currentStickerUuid.isNotBlank()) {
            DeleteWarningDialog(
                visible = openDeleteWarningDialog,
                { openDeleteWarningDialog = false },
                { openDeleteWarningDialog = false },
                {
                    openDeleteWarningDialog = false
                    viewModel.sendUiIntent(HomeIntent.DeleteStickerWithTags(currentStickerUuid))
                }
            )
        }
    }
}

@Composable
private fun RaysSearchBar(
    query: () -> String,
    onQueryChange: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val navController = LocalNavController.current
    val scope = rememberCoroutineScope()
    var active by rememberSaveable { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val searchBarHorizontalPadding: Dp by animateDpAsState(if (active) 0.dp else 16.dp)

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
                query = query(),
                onSearch = { keyword ->
                    keyboardController?.hide()
                    QueryPreference.put(context, scope, keyword)
                    viewModel.sendUiIntent(HomeIntent.GetStickerWithTagsList(keyword))
                },
                active = active,
                onActiveChange = {
                    active = it
                    if (!active) {
                        QueryPreference.put(context, scope, query())
                    }
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
                        if (query().isNotEmpty()) {
                            TrailingIcon(showClearButton = query().isNotEmpty()) {
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
                viewModel.uiStateFlow.collectAsStateWithLifecycle().value.apply {
                    when (searchResultUiState) {
                        SearchResultUiState.Init -> {}
                        is SearchResultUiState.Success -> {
                            SearchResultList(dataList = searchResultUiState.stickerWithTagsList,
                                onItemClickListener = {
                                    active = false
                                    viewModel.sendUiIntent(
                                        HomeIntent.GetStickerDetails(it.sticker.uuid)
                                    )
                                }
                            )
                        }
                    }
                }
            }
            HomeMenu(viewModel = viewModel)
        }
    }
}

@Composable
private fun TrailingIcon(
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
private fun SearchResultList(
    dataList: List<StickerWithTags>,
    onItemClickListener: ((data: StickerWithTags) -> Unit)? = null
) {
    Box {
        if (dataList.isEmpty()) {
            AnimatedPlaceholder(
                resId = R.raw.lottie_genshin_impact_klee_2,
                tip = stringResource(id = R.string.home_screen_no_search_result_tip)
            )
        } else {
            LazyVerticalStaggeredGrid(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 16.dp, horizontal = 16.dp),
                columns = StaggeredGridCells.Fixed(if (LocalContext.current.screenIsLand) 4 else 2),
                verticalItemSpacing = 12.dp,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items = dataList, key = { it.sticker.uuid }) {
                    SearchResultItem(data = it, onClickListener = onItemClickListener)
                }
            }
        }

        Badge(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 10.dp, end = 10.dp),
        ) {
            Text(text = dataList.size.toString())
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
        onLongClick = { context.sendSticker(data.sticker.uuid) },
        onClick = { onClickListener?.invoke(data) }
    ) {
        RaysImage(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
//                .heightIn(max = 300.dp),
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
private fun HomeMenu(viewModel: HomeViewModel) {
    val navController = LocalNavController.current
    val currentStickerUuid = LocalCurrentStickerUuid.current
    var editMenuItemEnabled by remember { mutableStateOf(false) }
    var deleteMenuItemEnabled by remember { mutableStateOf(false) }

    viewModel.uiStateFlow.collectAsStateWithLifecycle().value.apply {
        if (stickerDetailUiState is StickerDetailUiState.Success) {
            editMenuItemEnabled = true
            deleteMenuItemEnabled = true
        } else {
            editMenuItemEnabled = false
            deleteMenuItemEnabled = false
        }
    }

    DropdownMenu(
        expanded = menuExpanded,
        onDismissRequest = { menuExpanded = false }
    ) {
        DropdownMenuItem(
            enabled = editMenuItemEnabled,
            text = { Text(stringResource(R.string.home_screen_edit)) },
            onClick = {
                navController.navigate("$ADD_SCREEN_ROUTE?stickerUuid=${currentStickerUuid}")
                menuExpanded = false
            },
            leadingIcon = {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = null
                )
            }
        )
        DropdownMenuItem(
            enabled = deleteMenuItemEnabled,
            text = { Text(stringResource(R.string.home_screen_delete)) },
            onClick = {
                menuExpanded = false
                openDeleteWarningDialog = true
            },
            leadingIcon = {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null
                )
            }
        )
        Divider()
        DropdownMenuItem(
            text = { Text(stringResource(R.string.search_config_screen_name)) },
            onClick = {
                navController.navigate(SEARCH_CONFIG_SCREEN_ROUTE)
                menuExpanded = false
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
                        context.sendSticker(stickerBean.uuid)
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
                )
                RaysIconButton(
                    // align(Alignment.TopEnd) 无效，貌似是 PlainTooltipBox 的Bug
                    modifier = Modifier.align(Alignment.TopEnd),
                    style = RaysIconButtonStyle.FilledTonal,
                    imageVector = Icons.Default.Share,
                    contentDescription = stringResource(R.string.home_screen_send_sticker),
                    onClick = { context.sendSticker(stickerBean.uuid) }
                )
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
