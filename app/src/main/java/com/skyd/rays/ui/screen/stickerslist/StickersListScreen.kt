package com.skyd.rays.ui.screen.stickerslist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Deselect
import androidx.compose.material.icons.outlined.JoinLeft
import androidx.compose.material.icons.outlined.SelectAll
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.skyd.rays.R
import com.skyd.rays.base.mvi.MviEventListener
import com.skyd.rays.base.mvi.getDispatcher
import com.skyd.rays.ext.isCompact
import com.skyd.rays.model.bean.StickerWithTags
import com.skyd.rays.ui.component.PagingRefreshStateIndicator
import com.skyd.rays.ui.component.RaysIconButton
import com.skyd.rays.ui.component.RaysIconToggleButton
import com.skyd.rays.ui.component.RaysTopBar
import com.skyd.rays.ui.component.ScalableLazyVerticalStaggeredGrid
import com.skyd.rays.ui.component.dialog.WaitingDialog
import com.skyd.rays.ui.local.LocalNavController
import com.skyd.rays.ui.local.LocalWindowSizeClass
import com.skyd.rays.ui.screen.detail.DetailRoute
import com.skyd.rays.ui.screen.search.SearchResultItem
import com.skyd.rays.ui.screen.search.SearchResultItemPlaceholder
import com.skyd.rays.ui.screen.search.multiselect.MultiSelectActionBar
import kotlinx.serialization.Serializable


@Serializable
data class StickersListRoute(val query: String)

@Composable
fun StickersListScreen(query: String, viewModel: StickersListViewModel = hiltViewModel()) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val uiState by viewModel.viewState.collectAsStateWithLifecycle()
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    var multiSelect by rememberSaveable { mutableStateOf(false) }

    val dispatcher = viewModel.getDispatcher(startWith = StickersListIntent.GetStickersList(query))

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            RaysTopBar(
                scrollBehavior = scrollBehavior,
                title = {
                    Text(
                        text = query.ifBlank { stringResource(R.string.stickers_list_screen_name) },
                        modifier = Modifier
                            .run {
                                if (query.isBlank()) this
                                else clickable { clipboardManager.setText(AnnotatedString(query)) }
                            }
                            .basicMarquee(iterations = Int.MAX_VALUE),
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                actions = {
                    if (multiSelect) {
                        RaysIconButton(
                            onClick = {
                                dispatcher(
                                    StickersListIntent.InverseSelectedStickers(
                                        query = query, selectedStickers = uiState.selectedStickers,
                                    )
                                )
                            },
                            imageVector = Icons.Outlined.JoinLeft
                        )
                    }
                    RaysIconToggleButton(
                        checked = multiSelect,
                        onCheckedChange = {
                            multiSelect = it
                            if (!it) {
                                dispatcher(
                                    StickersListIntent.RemoveSelectedStickers(uiState.selectedStickers)
                                )
                            }
                        },
                    ) {
                        Icon(
                            if (multiSelect) Icons.Outlined.SelectAll else Icons.Outlined.Deselect,
                            contentDescription = null,
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        when (val listState = uiState.listState) {
            ListState.Init -> Unit
            is ListState.Success -> SuccessContent(
                lazyPagingItems = listState.stickerWithTagsPagingFlow.collectAsLazyPagingItems(),
                selectedStickers = uiState.selectedStickers,
                contentPadding = paddingValues,
                multiSelect = multiSelect,
                onSelectChanged = { data, selected ->
                    if (selected) {
                        dispatcher(StickersListIntent.AddSelectedStickers(listOf(data.sticker.uuid)))
                    } else {
                        dispatcher(StickersListIntent.RemoveSelectedStickers(listOf(data.sticker.uuid)))
                    }
                },
                onRemoveSelectedStickers = {
                    dispatcher(StickersListIntent.RemoveSelectedStickers(it))
                }
            )
        }

        MviEventListener(viewModel.singleEvent) { event ->
            when (event) {
                is StickersListEvent.InverseSelectedStickers.Failed -> snackbarHostState.showSnackbar(
                    context.getString(R.string.failed_info, event.msg)
                )
            }
        }

        WaitingDialog(visible = uiState.loadingDialog)
    }
}

@Composable
private fun SuccessContent(
    lazyPagingItems: LazyPagingItems<StickerWithTags>,
    selectedStickers: Set<String>,
    contentPadding: PaddingValues = PaddingValues(),
    multiSelect: Boolean,
    onSelectChanged: ((data: StickerWithTags, selected: Boolean) -> Unit)? = null,
    onRemoveSelectedStickers: (Collection<String>) -> Unit,
) {
    PagingRefreshStateIndicator(
        lazyPagingItems = lazyPagingItems,
        abnormalContent = { Box(modifier = Modifier.padding(contentPadding)) { it() } },
    ) {
        val navController = LocalNavController.current
        val isCompact = LocalWindowSizeClass.current.isCompact

        val multiSelectBar: @Composable (compact: Boolean) -> Unit = { compact ->
            AnimatedVisibility(
                visible = multiSelect,
                enter = if (compact) expandVertically() else expandHorizontally(),
                exit = if (compact) shrinkVertically() else shrinkHorizontally(),
            ) {
                MultiSelectActionBar(
                    selectedStickers = selectedStickers,
                    onRemoveSelectedStickers = onRemoveSelectedStickers,
                )
            }
        }

        val stickerList: @Composable () -> Unit = {
            StickerList(
                count = lazyPagingItems.itemCount,
                onData = { lazyPagingItems[it] },
                key = lazyPagingItems.itemKey { it.sticker.uuid },
                selectable = multiSelect,
                selected = { it.sticker.uuid in selectedStickers },
                contentPadding = PaddingValues(16.dp),
                onSelectChanged = onSelectChanged,
                onClick = { navController.navigate(DetailRoute(stickerUuid = it.sticker.uuid)) },
            )
        }

        if (isCompact) {
            Column(modifier = Modifier.padding(contentPadding)) {
                Box(modifier = Modifier.weight(1f)) { stickerList() }
                multiSelectBar(true)
            }
        } else {
            Row(modifier = Modifier.padding(contentPadding)) {
                multiSelectBar(false)
                stickerList()
            }
        }
    }
}

@Composable
fun StickerList(
    count: Int,
    onData: (Int) -> StickerWithTags?,
    key: ((index: Int) -> Any)? = null,
    selectable: Boolean = false,
    selected: (StickerWithTags) -> Boolean = { false },
    contentPadding: PaddingValues = PaddingValues(),
    onSelectChanged: ((data: StickerWithTags, selected: Boolean) -> Unit)? = null,
    onClick: ((data: StickerWithTags) -> Unit)? = null,
) {
    ScalableLazyVerticalStaggeredGrid(
        modifier = Modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalItemSpacing = 12.dp,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(count = count, key = key) {
            val data = onData(it)
            if (data == null) {
                SearchResultItemPlaceholder()
            } else {
                SearchResultItem(
                    data = data,
                    selectable = selectable,
                    selected = selected(data),
                    onSelectChanged = onSelectChanged,
                    onClick = onClick,
                )
            }
        }
    }
}