package com.skyd.rays.ui.screen.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.isContainer
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
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
import com.skyd.rays.model.bean.StickerWithTags1
import com.skyd.rays.model.preference.QueryPreference
import com.skyd.rays.model.preference.rememberQuery
import com.skyd.rays.ui.component.AnimatedPlaceholder
import com.skyd.rays.ui.component.RaysImage
import com.skyd.rays.ui.component.dialog.DeleteWarningDialog
import com.skyd.rays.ui.component.lazyverticalgrid.RaysLazyVerticalGrid
import com.skyd.rays.ui.component.lazyverticalgrid.adapter.LazyGridAdapter
import com.skyd.rays.ui.component.lazyverticalgrid.adapter.proxy.StickerWithTags1Proxy
import com.skyd.rays.ui.local.LocalCurrentStickerUuid
import com.skyd.rays.ui.local.LocalNavController
import com.skyd.rays.ui.screen.add.ADD_SCREEN_ROUTE
import com.skyd.rays.ui.screen.settings.searchconfig.SEARCH_CONFIG_SCREEN_ROUTE
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

            AnimatedVisibility(
                visible = stickerWithTags != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                stickerWithTags?.let {
                    MainCard(
                        stickerWithTags = it,
                        snackbarHostState = snackbarHostState
                    )
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
    val focusManager = LocalFocusManager.current
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
                .padding(bottom = 16.dp)
        ) {
            SearchBar(
                onQueryChange = onQueryChange,
                query = query(),
                onSearch = { keyword ->
                    focusManager.clearFocus()
                    keyboardController?.hide()
                    QueryPreference.put(context, scope, keyword)
                    viewModel.sendUiIntent(HomeIntent.GetStickerWithTagsList(keyword))
                },
                active = active,
                onActiveChange = {
                    active = it
                    if (!active) {
                        focusManager.clearFocus()
                        QueryPreference.put(context, scope, query())
                    }
                },
                placeholder = { Text(text = stringResource(R.string.home_screen_search_hint)) },
                leadingIcon = {
                    if (active) {
                        IconButton(onClick = {
                            focusManager.clearFocus()
                            active = false
                        }) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = stringResource(id = R.string.home_screen_close_search)
                            )
                        }
                    } else {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Default.Menu, contentDescription = null)
                        }
                    }
                },
                trailingIcon = {
                    if (active) {
                        IconButton(onClick = {
                            onQueryChange(QueryPreference.default)
                        }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = stringResource(R.string.home_screen_clear_search_text)
                            )
                        }
                    } else {
                        IconButton(onClick = {
                            navController.navigate(ADD_SCREEN_ROUTE)
                        }) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = stringResource(R.string.home_screen_add)
                            )
                        }
                    }
                },
            ) {
                viewModel.uiStateFlow.collectAsStateWithLifecycle().value.apply {
                    when (searchResultUiState) {
                        SearchResultUiState.Init -> {}
                        is SearchResultUiState.Success -> {
                            SearchResultList(dataList = searchResultUiState.stickerWithTagsList,
                                onItemClickListener = {
                                    focusManager.clearFocus()
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
private fun SearchResultList(
    dataList: List<Any>,
    onItemClickListener: ((data: StickerWithTags1) -> Unit)? = null
) {
    if (dataList.isEmpty()) {
        AnimatedPlaceholder(
            resId = R.raw.lottie_genshin_impact_klee_2,
            tip = stringResource(id = R.string.home_screen_no_search_result_tip)
        )
    }

    val adapter = remember {
        LazyGridAdapter(
            mutableListOf(
                StickerWithTags1Proxy(onClickListener = onItemClickListener)
            )
        )
    }
    RaysLazyVerticalGrid(
        modifier = Modifier.fillMaxSize(),
        dataList = dataList,
        adapter = adapter,
        contentPadding = PaddingValues(vertical = 7.dp)
    )
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
private fun MainCard(stickerWithTags: StickerWithTags, snackbarHostState: SnackbarHostState) {
    val navController = LocalNavController.current
    val currentStickerUuid = LocalCurrentStickerUuid.current
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

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
                    },
                    onDoubleClick = {
                        navController.navigate("$ADD_SCREEN_ROUTE?stickerUuid=${currentStickerUuid}")
                    },
                    onClick = {}
                )
        ) {
            RaysImage(
                modifier = Modifier.fillMaxWidth(),
                uuid = stickerBean.uuid,
            )
            if (stickerBean.title.isNotBlank()) {
                Text(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(top = 16.dp, bottom = if (tags.isEmpty()) 16.dp else 6.dp),
                    text = stickerBean.title,
                    style = MaterialTheme.typography.titleLarge
                )
            }
            if (tags.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 6.dp)
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
