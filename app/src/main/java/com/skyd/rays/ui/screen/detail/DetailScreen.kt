package com.skyd.rays.ui.screen.detail

import android.os.Bundle
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.AdsClick
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.skyd.rays.R
import com.skyd.rays.base.mvi.MviEventListener
import com.skyd.rays.base.mvi.getDispatcher
import com.skyd.rays.ext.isCompact
import com.skyd.rays.ext.navigate
import com.skyd.rays.ext.popBackStackWithLifecycle
import com.skyd.rays.ext.showSnackbar
import com.skyd.rays.ext.toDateTimeString
import com.skyd.rays.model.bean.StickerWithTags
import com.skyd.rays.model.bean.UriWithStickerUuidBean
import com.skyd.rays.model.preference.StickerScalePreference
import com.skyd.rays.model.preference.privacy.rememberShouldBlur
import com.skyd.rays.ui.component.AnimatedPlaceholder
import com.skyd.rays.ui.component.RadioTextItem
import com.skyd.rays.ui.component.RaysExtendedFloatingActionButton
import com.skyd.rays.ui.component.RaysFloatingActionButton
import com.skyd.rays.ui.component.RaysIconButton
import com.skyd.rays.ui.component.RaysImage
import com.skyd.rays.ui.component.RaysTopBar
import com.skyd.rays.ui.component.dialog.DeleteWarningDialog
import com.skyd.rays.ui.component.dialog.ExportDialog
import com.skyd.rays.ui.component.dialog.RaysDialog
import com.skyd.rays.ui.local.LocalNavController
import com.skyd.rays.ui.local.LocalStickerScale
import com.skyd.rays.ui.local.LocalWindowSizeClass
import com.skyd.rays.ui.screen.add.openAddScreen
import com.skyd.rays.ui.screen.fullimage.openFullImageScreen
import com.skyd.rays.ui.screen.search.imagesearch.openImageSearchScreen
import com.skyd.rays.ui.screen.stickerslist.openStickersListScreen
import com.skyd.rays.util.copyStickerToClipboard
import com.skyd.rays.util.sendStickerByUuid
import com.skyd.rays.util.stickerUuidToUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

const val DETAIL_SCREEN_ROUTE = "detailScreen"

fun openDetailScreen(
    navController: NavHostController,
    stickerUuid: String
) {
    navController.navigate(
        DETAIL_SCREEN_ROUTE,
        Bundle().apply {
            putString("stickerUuid", stickerUuid)
        }
    )
}

@Composable
fun DetailScreen(stickerUuid: String, viewModel: DetailViewModel = hiltViewModel()) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val scope = rememberCoroutineScope()
    val navController = LocalNavController.current
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var openMenu by rememberSaveable { mutableStateOf(false) }
    var openDeleteWarningDialog by rememberSaveable { mutableStateOf(false) }
    var openExportPathDialog by rememberSaveable { mutableStateOf(false) }
    var openStickerInfoDialog by rememberSaveable { mutableStateOf(false) }
    var openStickerScaleSheet by rememberSaveable { mutableStateOf(false) }
    val uiState by viewModel.viewState.collectAsStateWithLifecycle()
    val mainCardScrollState = rememberScrollState()
    val windowSizeClass = LocalWindowSizeClass.current
    var fabHeight by remember { mutableStateOf(0.dp) }

    val dispatch = viewModel.getDispatcher(
        startWith = DetailIntent.GetStickerDetailsAndAddClickCount(stickerUuid)
    )

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            DetailScreenFloatingActionButton(
                onClick = {
                    openAddScreen(
                        navController = navController,
                        stickers = listOf(
                            UriWithStickerUuidBean(
                                uri = stickerUuidToUri(stickerUuid),
                                stickerUuid = stickerUuid,
                            )
                        ),
                        isEdit = true
                    )
                },
                visible = uiState.stickerDetailState is StickerDetailState.Success,
                onSizeWithSinglePaddingChanged = { _, height -> fabHeight = height }
            )
        },
        topBar = {
            RaysTopBar(
                scrollBehavior = scrollBehavior,
                title = {
                    val stickerDetailState = uiState.stickerDetailState
                    val title = if (stickerDetailState !is StickerDetailState.Success) ""
                    else stickerDetailState.stickerWithTags.sticker.title
                    Text(
                        modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
                        text = title.ifBlank { stringResource(R.string.detail_screen_name) },
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                actions = {
                    RaysIconButton(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = stringResource(R.string.send_sticker),
                        enabled = uiState.stickerDetailState is StickerDetailState.Success,
                        onClick = {
                            context.sendStickerByUuid(
                                uuid = stickerUuid,
                                onSuccess = {
                                    val stickerDetailState = uiState.stickerDetailState
                                    if (stickerDetailState is StickerDetailState.Success) {
                                        stickerDetailState.stickerWithTags.sticker.clickCount++
                                    }
                                }
                            )
                        },
                    )
                    RaysIconButton(
                        imageVector = Icons.Outlined.ContentCopy,
                        contentDescription = stringResource(id = R.string.detail_screen_copy),
                        enabled = uiState.stickerDetailState is StickerDetailState.Success,
                        onClick = {
                            scope.launch(Dispatchers.IO) {
                                context.copyStickerToClipboard(uuid = stickerUuid)
                            }
                            snackbarHostState.showSnackbar(
                                scope = scope,
                                message = context.getString(R.string.detail_screen_copied)
                            )
                        }
                    )
                    RaysIconButton(
                        onClick = { openMenu = true },
                        enabled = uiState.stickerDetailState is StickerDetailState.Success,
                        imageVector = Icons.Outlined.MoreVert,
                        contentDescription = stringResource(id = R.string.more)
                    )
                    DetailMenu(
                        expanded = openMenu,
                        stickerMenuItemEnabled = uiState.stickerDetailState !is StickerDetailState.Init,
                        onDismissRequest = { openMenu = false },
                        onDeleteClick = { openDeleteWarningDialog = true },
                        onExportClick = { openExportPathDialog = true },
                        onStickerInfoClick = { openStickerInfoDialog = true },
                        onFullImageClick = {
                            val state = uiState.stickerDetailState as? StickerDetailState.Success
                                ?: return@DetailMenu
                            openFullImageScreen(
                                navController = navController,
                                image = stickerUuidToUri(state.stickerWithTags.sticker.uuid)
                            )
                        },
                        onStickerSearchClick = {
                            val state = uiState.stickerDetailState as? StickerDetailState.Success
                                ?: return@DetailMenu
                            openImageSearchScreen(
                                navController = navController,
                                baseImage = stickerUuidToUri(state.stickerWithTags.sticker.uuid),
                            )
                        },
                        onStickerScaleClick = { openStickerScaleSheet = true },
                    )
                },
            )
        }
    ) { paddingValues ->
        Row(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            val stickerDetailUiState = uiState.stickerDetailState
            val showStickerDetailInfo = !windowSizeClass.isCompact &&
                    stickerDetailUiState is StickerDetailState.Success

            Column(modifier = Modifier.weight(1f)) {
                when (stickerDetailUiState) {
                    StickerDetailState.Init -> DetailScreenEmptyPlaceholder()
                    StickerDetailState.Loading -> DetailScreenLoadingPlaceholder()

                    is StickerDetailState.Success -> {
                        val stickerWithTags = stickerDetailUiState.stickerWithTags
                        MainCard(
                            stickerWithTags = stickerWithTags,
                            scrollState = mainCardScrollState,
                            bottomPadding = if (windowSizeClass.isCompact) fabHeight else 0.dp,
                        )
                        RaysDialog(
                            visible = openStickerInfoDialog,
                            title = { Text(text = stringResource(id = R.string.detail_screen_sticker_info)) },
                            text = { StickerDetailInfo(stickerWithTags = stickerWithTags) },
                            confirmButton = {
                                TextButton(onClick = { openStickerInfoDialog = false }) {
                                    Text(text = stringResource(id = R.string.dialog_ok))
                                }
                            },
                            onDismissRequest = { openStickerInfoDialog = false }
                        )
                    }
                }
            }
            StickerDetailInfoCard(
                visible = showStickerDetailInfo,
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .padding(end = 16.dp, top = 16.dp, bottom = 16.dp + fabHeight),
                stickerWithTags = {
                    (stickerDetailUiState as StickerDetailState.Success).stickerWithTags
                },
            )
        }

        MviEventListener(viewModel.singleEvent) { event ->
            when (event) {
                is DetailEvent.DeleteResult.Success -> navController.popBackStackWithLifecycle()

                DetailEvent.ExportResult.Success ->
                    snackbarHostState.showSnackbar(context.getString(R.string.export_sticker_success))

                DetailEvent.ExportResult.Failed ->
                    snackbarHostState.showSnackbar(context.getString(R.string.export_sticker_failed))

                DetailEvent.DeleteResult.Failed ->
                    snackbarHostState.showSnackbar(context.getString(R.string.delete_sticker_failed))
            }
        }

        DeleteWarningDialog(
            visible = openDeleteWarningDialog,
            onDismissRequest = { openDeleteWarningDialog = false },
            onDismiss = { openDeleteWarningDialog = false },
            onConfirm = {
                openDeleteWarningDialog = false
                dispatch(DetailIntent.DeleteStickerWithTags(stickerUuid))
            }
        )

        ExportDialog(
            visible = openExportPathDialog,
            onDismissRequest = { openExportPathDialog = false },
            onExport = { dispatch(DetailIntent.ExportStickers(stickerUuid)) },
        )

        if (openStickerScaleSheet) {
            StickerScaleSheet {
                openStickerScaleSheet = false
            }
        }
    }
}

@Composable
fun MainCard(
    stickerWithTags: StickerWithTags,
    scrollState: ScrollState = rememberScrollState(),
    bottomPadding: Dp = 0.dp,
) {
    val navController = LocalNavController.current
    val context = LocalContext.current

    val stickerBean = stickerWithTags.sticker
    val tags = stickerWithTags.tags

    Card(
        modifier = Modifier
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp, bottom = bottomPadding + 16.dp)
            .fillMaxWidth()
    ) {
        val shouldBlur = rememberShouldBlur(tags.map { it.tag } + stickerBean.title)
        var blur by rememberSaveable { mutableStateOf(shouldBlur) }
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
                            stickers = listOf(
                                UriWithStickerUuidBean(
                                    uri = stickerUuidToUri(stickerBean.uuid),
                                    stickerUuid = stickerBean.uuid,
                                )
                            ),
                            isEdit = true,
                        )
                    },
                    onClick = {
                        if (blur) blur = false else {
                            openFullImageScreen(
                                navController = navController,
                                image = stickerUuidToUri(stickerBean.uuid)
                            )
                        }
                    }
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
                    blur = blur,
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
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    repeat(tags.size) { index ->
                        val interactionSource = remember { MutableInteractionSource() }
                        AssistChip(
                            onClick = {
                                openStickersListScreen(
                                    navController = navController,
                                    query = tags[index].tag,
                                )
                            },
                            label = { Text(tags[index].tag) },
                            interactionSource = interactionSource,
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
            icon = Icons.Outlined.Badge,
            title = stringResource(id = R.string.detail_screen_sticker_info_uuid),
            text = sticker.uuid,
        )
        DetailInfoItem(
            icon = Icons.Outlined.Image,
            title = stringResource(id = R.string.detail_screen_sticker_info_md5),
            text = sticker.stickerMd5,
        )
        DetailInfoItem(
            icon = Icons.Outlined.AdsClick,
            title = stringResource(id = R.string.sticker_click_count),
            text = sticker.clickCount.toString(),
        )
        DetailInfoItem(
            icon = Icons.Outlined.Share,
            title = stringResource(id = R.string.sticker_share_count),
            text = sticker.shareCount.toString()
        )
        DetailInfoItem(
            icon = Icons.Outlined.AddCircle,
            title = stringResource(id = R.string.sticker_create_time),
            text = sticker.createTime.toDateTimeString()
        )
        DetailInfoItem(
            icon = Icons.Outlined.Edit,
            title = stringResource(id = R.string.detail_screen_sticker_info_last_modified_time),
            text = (sticker.modifyTime ?: sticker.createTime).toDateTimeString()
        )
    }
}

@Composable
fun StickerDetailInfoCard(
    visible: Boolean,
    modifier: Modifier,
    stickerWithTags: () -> StickerWithTags
) {
    AnimatedVisibility(
        visible = visible,
        modifier = Modifier
            .fillMaxHeight()
            .verticalScroll(rememberScrollState()),
        enter = expandHorizontally(expandFrom = Alignment.Start),
        exit = shrinkHorizontally(shrinkTowards = Alignment.End),
    ) {
        Card(modifier = modifier) {
            StickerDetailInfo(
                modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
                stickerWithTags = stickerWithTags()
            )
        }
    }
}

@Composable
fun DetailScreenFloatingActionButton(
    onClick: () -> Unit,
    visible: Boolean = true,
    onSizeWithSinglePaddingChanged: ((width: Dp, height: Dp) -> Unit)
) {
    val windowSizeClass = LocalWindowSizeClass.current

    val content: @Composable () -> Unit = remember {
        @Composable {
            Icon(imageVector = Icons.Outlined.Edit, contentDescription = null)
        }
    }

    if (visible) {
        if (windowSizeClass.isCompact) {
            RaysFloatingActionButton(
                content = { content() },
                onClick = onClick,
                onSizeWithSinglePaddingChanged = onSizeWithSinglePaddingChanged,
                contentDescription = stringResource(R.string.detail_screen_edit),
            )
        } else {
            RaysExtendedFloatingActionButton(
                text = { Text(text = stringResource(R.string.detail_screen_edit)) },
                icon = content,
                onClick = onClick,
                onSizeWithSinglePaddingChanged = onSizeWithSinglePaddingChanged,
                contentDescription = stringResource(R.string.detail_screen_edit),
            )
        }
    }
}

@Composable
fun DetailScreenEmptyPlaceholder() {
    AnimatedPlaceholder(
        resId = R.raw.lottie_genshin_impact_keqing_1,
        tip = stringResource(id = R.string.detail_screen_empty_tip)
    )
}

@Composable
fun DetailScreenLoadingPlaceholder() {
    AnimatedPlaceholder(
        resId = R.raw.lottie_genshin_impact_klee_3,
        tip = stringResource(id = R.string.loading)
    )
}

@Composable
private fun StickerScaleSheet(onDismissRequest: () -> Unit) {
    val bottomSheetState = rememberModalBottomSheetState()
    val stickerScale = LocalStickerScale.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = bottomSheetState,
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
                .selectableGroup()
        ) {
            StickerScalePreference.scaleList.forEach {
                RadioTextItem(
                    text = StickerScalePreference.toDisplayName(it),
                    selected = (it == stickerScale),
                    onClick = {
                        StickerScalePreference.put(
                            context = context, scope = scope, value = it
                        )
                    },
                )
            }
        }
    }
}