package com.skyd.rays.ui.screen.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AdsClick
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skyd.rays.R
import com.skyd.rays.base.LoadUiIntent
import com.skyd.rays.config.refreshStickerData
import com.skyd.rays.ext.dateTime
import com.skyd.rays.ext.showSnackbarWithLaunchedEffect
import com.skyd.rays.model.bean.StickerWithTags
import com.skyd.rays.model.bean.UriWithStickerUuidBean
import com.skyd.rays.model.preference.StickerScalePreference
import com.skyd.rays.model.preference.search.QueryPreference
import com.skyd.rays.ui.component.AnimatedPlaceholder
import com.skyd.rays.ui.component.RaysExtendedFloatingActionButton
import com.skyd.rays.ui.component.RaysIconButton
import com.skyd.rays.ui.component.RaysIconButtonStyle
import com.skyd.rays.ui.component.RaysImage
import com.skyd.rays.ui.component.dialog.WaitingDialog
import com.skyd.rays.ui.local.LocalCurrentStickerUuid
import com.skyd.rays.ui.local.LocalHomeShareButtonAlignment
import com.skyd.rays.ui.local.LocalNavController
import com.skyd.rays.ui.local.LocalQuery
import com.skyd.rays.ui.local.LocalStickerScale
import com.skyd.rays.ui.local.LocalWindowSizeClass
import com.skyd.rays.ui.screen.add.openAddScreen
import com.skyd.rays.ui.screen.home.searchbar.RaysSearchBar
import com.skyd.rays.util.sendStickerByUuid
import com.skyd.rays.util.stickerUuidToUri


@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val navController = LocalNavController.current
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val currentStickerUuid = LocalCurrentStickerUuid.current
    val windowSizeClass = LocalWindowSizeClass.current
    val initQuery = LocalQuery.current
    var active by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable(initQuery) { mutableStateOf(initQuery) }
    var openWaitingDialog by rememberSaveable { mutableStateOf(false) }
    val uiState by viewModel.uiStateFlow.collectAsStateWithLifecycle()
    val uiEvent by viewModel.uiEventFlow.collectAsStateWithLifecycle(initialValue = null)
    val loadUiIntent by viewModel.loadUiIntentFlow.collectAsStateWithLifecycle(initialValue = null)
    val mainCardScrollState = rememberScrollState()
    val stickerDetailInfoScrollState = rememberScrollState()
    var fabHeight by remember { mutableStateOf(0.dp) }

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
        floatingActionButton = {
            AnimatedVisibility(
                visible = !active,
                enter = slideInVertically { with(density) { 40.dp.roundToPx() } } + fadeIn(),
                exit = slideOutVertically { with(density) { 40.dp.roundToPx() } } + fadeOut(),
            ) {
                RaysExtendedFloatingActionButton(
                    text = { Text(text = stringResource(R.string.home_screen_add)) },
                    icon = { Icon(imageVector = Icons.Default.Add, contentDescription = null) },
                    onClick = {
                        openAddScreen(
                            navController = navController,
                            stickers = mutableListOf(),
                            isEdit = false,
                        )
                    },
                    onSizeWithSinglePaddingChanged = { _, height -> fabHeight = height },
                    contentDescription = stringResource(R.string.home_screen_add),
                )
            }
        },
        contentWindowInsets = WindowInsets(
            left = ScaffoldDefaults.contentWindowInsets
                .getLeft(LocalDensity.current, LocalLayoutDirection.current),
            top = 0,
            right = ScaffoldDefaults.contentWindowInsets
                .getRight(LocalDensity.current, LocalLayoutDirection.current),
            bottom = ScaffoldDefaults.contentWindowInsets.getBottom(LocalDensity.current),
        )
    ) { innerPaddings ->
        Row(
            modifier = Modifier
                .padding(innerPaddings)
                .fillMaxSize()
        ) {
            val stickerDetailUiState = uiState.stickerDetailUiState
            val showStickerDetailInfo = !active &&
                    windowSizeClass.widthSizeClass != WindowWidthSizeClass.Compact &&
                    stickerDetailUiState is StickerDetailUiState.Success

            Column(modifier = Modifier.weight(1f)) {
                RaysSearchBar(
                    query = query,
                    onQueryChange = {
                        query = it
                        QueryPreference.put(context, scope, it)
                    },
                    active = active,
                    onActiveChange = {
                        active = it
                    },
                    stickerWithTags = (stickerDetailUiState as? StickerDetailUiState.Success)
                        ?.stickerWithTags,
                    uiState = uiState,
                )
                when (stickerDetailUiState) {
                    is StickerDetailUiState.Init -> {
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
                        Spacer(modifier = Modifier.height(16.dp))
                        MainCard(
                            stickerWithTags = stickerDetailUiState.stickerWithTags,
                            scrollState = mainCardScrollState,
                            bottomPadding = fabHeight,
                        )
                    }
                }
            }
            AnimatedVisibility(
                visible = showStickerDetailInfo,
                modifier = Modifier
                    .fillMaxHeight()
                    .verticalScroll(stickerDetailInfoScrollState),
                enter = expandHorizontally(expandFrom = Alignment.Start),
                exit = shrinkHorizontally(shrinkTowards = Alignment.End),
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.4f)
                        .padding(end = 16.dp, top = 8.dp, bottom = 16.dp)
                        .statusBarsPadding()
                ) {
                    StickerDetailInfo(
                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
                        stickerWithTags = (stickerDetailUiState as StickerDetailUiState.Success)
                            .stickerWithTags
                    )
                }
            }
        }

        uiEvent?.apply {
            when (homeResultUiEvent) {
                is HomeResultUiEvent.Success -> {
                    snackbarHostState.showSnackbarWithLaunchedEffect(
                        message = context.resources.getQuantityString(
                            R.plurals.home_screen_export_result,
                            homeResultUiEvent.successCount,
                            homeResultUiEvent.successCount
                        ),
                        key2 = homeResultUiEvent,
                    )
                }

                null -> Unit
            }
        }

        loadUiIntent?.also { loadUiIntent ->
            when (loadUiIntent) {
                is LoadUiIntent.Error -> {
                    snackbarHostState.showSnackbarWithLaunchedEffect(
                        message = context.getString(
                            R.string.home_screen_failed, loadUiIntent.msg
                        ),
                        key2 = loadUiIntent,
                    )
                }

                is LoadUiIntent.Loading -> openWaitingDialog = loadUiIntent.isShow
            }
        }

        WaitingDialog(visible = openWaitingDialog)
    }
}

@Composable
private fun MainCard(
    stickerWithTags: StickerWithTags,
    scrollState: ScrollState = rememberScrollState(),
    bottomPadding: Dp = 0.dp,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val navController = LocalNavController.current
    val stickerUuid = stickerWithTags.sticker.uuid
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val primaryColor = MaterialTheme.colorScheme.primary

    val stickerBean = stickerWithTags.sticker
    val tags = stickerWithTags.tags

    LaunchedEffect(stickerUuid) {
        viewModel.sendUiIntent(
            HomeIntent.UpdateThemeColor(
                stickerUuid = stickerUuid,
                primaryColor = primaryColor.toArgb(),
            )
        )
    }

    Card(
        modifier = Modifier
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp)
            .padding(bottom = bottomPadding + 16.dp)
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .combinedClickable(
                    onLongClick = {
                        context.sendStickerByUuid(
                            uuid = stickerBean.uuid,
                            onSuccess = { stickerBean.shareCount++ }
                        )
                    },
                    onDoubleClick = {
                        openAddScreen(
                            navController = navController,
                            stickers = mutableListOf(
                                UriWithStickerUuidBean(
                                    uri = stickerUuidToUri(stickerBean.uuid),
                                    stickerUuid = stickerBean.uuid,
                                )
                            ),
                            isEdit = true,
                        )
                    },
                    onClick = {}
                )
        ) {
            Box {
                RaysImage(
                    modifier = Modifier
                        .animateContentSize(
                            animationSpec = spring(
                                dampingRatio = 1.3f,
                                stiffness = Spring.StiffnessHigh,
                            )
                        )
                        .fillMaxWidth(),
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
                            context.sendStickerByUuid(
                                uuid = stickerBean.uuid,
                                onSuccess = { stickerBean.shareCount++ }
                            )
                        },
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

@Composable
fun StickerDetailInfo(modifier: Modifier = Modifier, stickerWithTags: StickerWithTags) {
    @Composable
    fun DetailInfoItem(icon: ImageVector, title: String, text: String) {
        Row(
            modifier = Modifier.padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null)
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.labelSmall)
                SelectionContainer {
                    Text(text = text, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }

    val sticker = stickerWithTags.sticker
    Column(modifier = modifier) {
        DetailInfoItem(
            icon = Icons.Default.Badge,
            title = stringResource(id = R.string.home_screen_sticker_info_uuid),
            text = sticker.uuid,
        )
        DetailInfoItem(
            icon = Icons.Default.Image,
            title = stringResource(id = R.string.home_screen_sticker_info_md5),
            text = sticker.stickerMd5,
        )
        DetailInfoItem(
            icon = Icons.Default.AdsClick,
            title = stringResource(id = R.string.home_screen_sticker_info_click_count),
            text = sticker.clickCount.toString(),
        )
        DetailInfoItem(
            icon = Icons.Default.Share,
            title = stringResource(id = R.string.home_screen_sticker_info_share_count),
            text = sticker.shareCount.toString()
        )
        DetailInfoItem(
            icon = Icons.Default.AddCircle,
            title = stringResource(id = R.string.home_screen_sticker_info_create_time),
            text = dateTime(sticker.createTime)
        )
        DetailInfoItem(
            icon = Icons.Default.Edit,
            title = stringResource(id = R.string.home_screen_sticker_info_last_modified_time),
            text = sticker.modifyTime?.let { dateTime(it) } ?: dateTime(sticker.createTime)
        )
    }
}