package com.skyd.rays.ui.screen.mergestickers

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Merge
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material.icons.outlined.Title
import androidx.compose.material.icons.outlined.ZoomOutMap
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skyd.rays.R
import com.skyd.rays.base.mvi.MviEventListener
import com.skyd.rays.base.mvi.getDispatcher
import com.skyd.rays.ext.popBackStackWithLifecycle
import com.skyd.rays.model.bean.StickerWithTags
import com.skyd.rays.model.bean.TagBean
import com.skyd.rays.ui.component.CircularProgressPlaceholder
import com.skyd.rays.ui.component.ErrorPlaceholder
import com.skyd.rays.ui.component.RaysExtendedFloatingActionButton
import com.skyd.rays.ui.component.RaysIconButton
import com.skyd.rays.ui.component.RaysImage
import com.skyd.rays.ui.component.RaysTopBar
import com.skyd.rays.ui.component.dialog.RaysDialog
import com.skyd.rays.ui.component.dialog.WaitingDialog
import com.skyd.rays.ui.local.LocalNavController
import com.skyd.rays.ui.screen.fullimage.FullImageRoute
import com.skyd.rays.util.stickerUuidToUri
import kotlinx.serialization.Serializable
import java.util.UUID


@Serializable
data class MergeStickersRoute(val stickerUuids: List<String>)

@Composable
fun MergeStickersScreenRoute(stickerUuids: List<String>) {
    val context = LocalContext.current
    val navController = LocalNavController.current
    var dialogOpened by rememberSaveable { mutableStateOf(false) }
    var openDialog by rememberSaveable { mutableStateOf<String?>(null) }
    var onDismiss = {}
    LaunchedEffect(Unit) {
        if (dialogOpened) return@LaunchedEffect
        if (stickerUuids.isEmpty()) {
            openDialog = context.getString(R.string.merge_stickers_screen_empty_sticker_list)
            onDismiss = { navController.popBackStackWithLifecycle() }
        } else if (stickerUuids.size > 20) {
            openDialog = context.getString(R.string.merge_stickers_screen_max_count_limit)
        }
        dialogOpened = true
    }
    if (stickerUuids.isNotEmpty()) {
        if (stickerUuids.size > 20) {
            MergeStickersScreen(stickerUuids.subList(0, 20))
        } else {
            MergeStickersScreen(stickerUuids)
        }
    }
    if (openDialog != null) {
        val onDismissRequest = {
            openDialog = null
            onDismiss()
        }
        RaysDialog(
            title = { Text(stringResource(R.string.info)) },
            text = { Text(openDialog.orEmpty()) },
            onDismissRequest = onDismissRequest,
            confirmButton = {
                TextButton(onClick = onDismissRequest) {
                    Text(stringResource(R.string.dialog_ok))
                }
            }
        )
    }
}

@Composable
fun MergeStickersScreen(
    stickerUuids: List<String>,
    viewModel: MergeStickersViewModel = hiltViewModel(),
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val navController = LocalNavController.current
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val uiState by viewModel.viewState.collectAsStateWithLifecycle()
    var fabHeight by remember { mutableStateOf(0.dp) }

    var selectedStickerUuid by rememberSaveable { mutableStateOf("") }
    var title by rememberSaveable { mutableStateOf("") }

    var openMergeSuccessDialog by rememberSaveable { mutableStateOf(false) }

    val dispatch = viewModel.getDispatcher(startWith = MergeStickersIntent.Init(stickerUuids))

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            val stickersState = uiState.stickersState
            if (stickersState is StickersState.Success &&
                selectedStickerUuid.isNotBlank()
            ) {
                RaysExtendedFloatingActionButton(
                    text = { Text(text = stringResource(R.string.merge_stickers_screen_merge)) },
                    icon = { Icon(Icons.Outlined.Merge, contentDescription = null) },
                    onClick = {
                        val stickerWithTags = stickersState.stickersList.first {
                            it.sticker.uuid == selectedStickerUuid
                        }.run {
                            copy(
                                sticker = sticker.copy(
                                    uuid = UUID.randomUUID().toString(),
                                    title = title,
                                    createTime = System.currentTimeMillis(),
                                ),
                                tags = uiState.selectedTags.distinct().map { TagBean(it) },
                            )
                        }
                        dispatch(
                            MergeStickersIntent.Merge(
                                oldStickerUuid = selectedStickerUuid,
                                sticker = stickerWithTags,
                                deleteUuids = stickersState.stickersList.map { it.sticker.uuid },
                            )
                        )
                    },
                    onSizeWithSinglePaddingChanged = { _, height -> fabHeight = height },
                    contentDescription = stringResource(R.string.detail_screen_edit),
                )
            }
        },
        topBar = {
            RaysTopBar(
                scrollBehavior = scrollBehavior,
                title = { Text(text = stringResource(R.string.merge_stickers_screen_name)) },
            )
        }
    ) { paddingValues ->
        when (val stickersState = uiState.stickersState) {
            StickersState.Init -> CircularProgressPlaceholder(contentPadding = paddingValues)
            is StickersState.Failed -> ErrorPlaceholder(
                text = stickersState.msg,
                contentPadding = paddingValues,
            )

            is StickersState.Success -> SuccessContent(
                stickersState = stickersState,
                contentPadding = paddingValues,
                selectedStickerUuid = selectedStickerUuid,
                onSelectedStickerChanged = { selectedStickerUuid = it.sticker.uuid },
                selectedTitle = title,
                onSelectedTitleChanged = { title = it },
                selectedTags = uiState.selectedTags,
                onSelectedTagChanged = { tag, selected ->
                    if (selected) {
                        if (tag !in uiState.selectedTags) {
                            dispatch(MergeStickersIntent.AddSelectedTag(tag))
                        }
                    } else {
                        if (tag in uiState.selectedTags) {
                            dispatch(MergeStickersIntent.RemoveSelectedTag(tag))
                        }
                    }
                },
                onReplaceSelectedTags = { dispatch(MergeStickersIntent.ReplaceAllSelectedTags(it)) }
            )
        }

        MviEventListener(viewModel.singleEvent) { event ->
            when (event) {
                is MergeStickersEvent.MergeResult.Failed -> snackbarHostState.showSnackbar(
                    context.getString(R.string.failed_info, event.msg)
                )

                MergeStickersEvent.MergeResult.Success -> openMergeSuccessDialog = true
            }
        }

        if (openMergeSuccessDialog) {
            RaysDialog(
                title = { Text(stringResource(R.string.merge_stickers_screen_merge_successful)) },
                confirmButton = {
                    TextButton(onClick = { navController.popBackStackWithLifecycle() }) {
                        Text(stringResource(R.string.dialog_ok))
                    }
                }
            )
        }

        WaitingDialog(visible = uiState.loadingDialog)
    }
}

@Composable
private fun SuccessContent(
    stickersState: StickersState.Success,
    contentPadding: PaddingValues = PaddingValues(),
    selectedStickerUuid: String,
    onSelectedStickerChanged: (StickerWithTags) -> Unit,
    selectedTitle: String,
    onSelectedTitleChanged: (String) -> Unit,
    selectedTags: List<String>,
    onSelectedTagChanged: (String, Boolean) -> Unit,
    onReplaceSelectedTags: (List<String>) -> Unit,
) {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(contentPadding),
    ) {
        StickerImages(
            stickersList = stickersState.stickersList,
            selectedStickerUuid = selectedStickerUuid,
            onSelectedStickerChanged = onSelectedStickerChanged,
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 15.dp))
        FilterClips(
            title = stringResource(R.string.merge_stickers_screen_candidate_titles),
            imageVector = Icons.Outlined.Title,
            dataList = stickersState.stickersList.map { it.sticker.title },
            selectedList = listOf(selectedTitle),
            onSelectedChanged = { text, selected ->
                onSelectedTitleChanged(if (selected) text else "")
            },
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 15.dp))
        FilterClips(
            title = stringResource(R.string.merge_stickers_screen_candidate_tags),
            imageVector = Icons.Outlined.Sell,
            dataList = stickersState.stickersList.map { sticker ->
                sticker.tags.map { it.tag }
            }.flatten(),
            selectedList = selectedTags,
            onSelectedChanged = onSelectedTagChanged,
            onReplaceSelectedTags = onReplaceSelectedTags,
        )
    }
}

@Composable
fun StickerImages(
    stickersList: List<StickerWithTags>,
    selectedStickerUuid: String,
    onSelectedStickerChanged: (StickerWithTags) -> Unit,
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Image, contentDescription = null)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = stringResource(R.string.merge_stickers_screen_candidate_images),
                style = MaterialTheme.typography.titleMedium,
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(
            modifier = Modifier.height(150.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(stickersList) { item ->
                val selected = selectedStickerUuid == item.sticker.uuid
                Card(
                    modifier = Modifier.clickable { onSelectedStickerChanged(item) },
                    border = if (selected) {
                        BorderStroke(6.dp, MaterialTheme.colorScheme.primary)
                    } else null,
                ) {
                    Box {
                        RaysImage(
                            uuid = item.sticker.uuid,
                            blur = false,
                            modifier = Modifier
                                .widthIn(min = 30.dp, max = 120.dp)
                                .fillMaxHeight(),
                        )
                        val navController = LocalNavController.current
                        RaysIconButton(
                            modifier = Modifier.align(Alignment.TopEnd),
                            onClick = {
                                navController.navigate(FullImageRoute(image = stickerUuidToUri(item.sticker.uuid)))
                            },
                            imageVector = Icons.Outlined.ZoomOutMap,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterClips(
    title: String,
    imageVector: ImageVector,
    dataList: List<String>,
    selectedList: List<String>,
    onSelectedChanged: (String, Boolean) -> Unit,
    onReplaceSelectedTags: ((List<String>) -> Unit)? = null,
) {
    val distinctList = rememberSaveable(dataList) {
        dataList.distinct().filter { it.isNotBlank() }
    }
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector, contentDescription = null)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium,
            )
            if (onReplaceSelectedTags != null) {
                TextButton(onClick = { onReplaceSelectedTags(dataList - selectedList.toSet()) }) {
                    Text(stringResource(R.string.invert_selection))
                }
            }
        }
        if (onReplaceSelectedTags == null) {
            Spacer(modifier = Modifier.height(6.dp))
        }
        if (distinctList.isEmpty()) {
            Text(
                text = stringResource(R.string.empty_tip),
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        } else {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                repeat(distinctList.size) { index ->
                    val text = distinctList[index]
                    val selected = text in selectedList
                    FilterChip(
                        selected = selected,
                        onClick = { onSelectedChanged(text, !selected) },
                        label = { Text(text) },
                        leadingIcon = {
                            AnimatedVisibility(selected) {
                                Icon(Icons.Outlined.Done, contentDescription = null)
                            }
                        }
                    )
                }
            }
        }
    }
}