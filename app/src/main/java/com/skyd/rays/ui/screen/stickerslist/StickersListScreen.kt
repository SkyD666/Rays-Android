package com.skyd.rays.ui.screen.stickerslist

import android.os.Bundle
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FolderZip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.paging.compose.collectAsLazyPagingItems
import com.skyd.rays.R
import com.skyd.rays.base.mvi.MviEventListener
import com.skyd.rays.base.mvi.getDispatcher
import com.skyd.rays.ext.collectAsLazyPagingItemsWithLifecycle
import com.skyd.rays.ext.navigate
import com.skyd.rays.ext.plus
import com.skyd.rays.model.bean.StickerWithTags
import com.skyd.rays.ui.component.RaysIconButton
import com.skyd.rays.ui.component.RaysTopBar
import com.skyd.rays.ui.component.ScalableLazyVerticalStaggeredGrid
import com.skyd.rays.ui.local.LocalNavController
import com.skyd.rays.ui.screen.detail.openDetailScreen
import com.skyd.rays.ui.screen.search.SearchResultItem
import com.skyd.rays.ui.screen.settings.data.importexport.file.exportfiles.openExportFilesScreen

const val STICKERS_LIST_SCREEN_ROUTE = "stickersListScreen"

fun openStickersListScreen(
    navController: NavHostController,
    query: String
) {
    navController.navigate(
        STICKERS_LIST_SCREEN_ROUTE,
        Bundle().apply {
            putString("query", query)
        }
    )
}

@Composable
fun StickersListScreen(query: String, viewModel: StickersListViewModel = hiltViewModel()) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val navController = LocalNavController.current
    val uiState by viewModel.viewState.collectAsStateWithLifecycle()
    val clipboardManager: ClipboardManager = LocalClipboardManager.current

    val dispatcher = viewModel.getDispatcher(startWith = StickersListIntent.GetStickersList(query))

    Scaffold(
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
                    val lazyPagingItems = (uiState.listState as? ListState.Success)
                        ?.stickerWithTagsPagingFlow
                        ?.collectAsLazyPagingItems()
                    RaysIconButton(
                        enabled = (lazyPagingItems?.itemCount ?: 0) > 0,
                        onClick = { dispatcher(StickersListIntent.ExportStickers(query)) },
                        imageVector = Icons.Outlined.FolderZip,
                        contentDescription = stringResource(id = R.string.stickers_list_screen_export_current_stickers),
                    )
                }
            )
        }
    ) { paddingValues ->
        when (val listState = uiState.listState) {
            ListState.Init -> Unit
            is ListState.Success -> {
                val lazyPagingItems =
                    listState.stickerWithTagsPagingFlow.collectAsLazyPagingItemsWithLifecycle()
                StickerList(
                    count = lazyPagingItems.itemCount,
                    data = { lazyPagingItems[it]!! },
                    key = { lazyPagingItems[it]!!.sticker.uuid },
                    contentPadding = paddingValues + PaddingValues(16.dp),
                )
            }
        }

        MviEventListener(viewModel.singleEvent) { event ->
            when (event) {
                is StickersListEvent.ExportStickers.Success -> openExportFilesScreen(
                    navController = navController,
                    exportStickers = event.stickerUuids,
                )
            }
        }
    }
}

@Composable
fun StickerList(
    count: Int,
    data: (Int) -> StickerWithTags,
    key: ((index: Int) -> Any)? = null,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val navController = LocalNavController.current
    ScalableLazyVerticalStaggeredGrid(
        modifier = Modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalItemSpacing = 12.dp,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(count = count, key = key) {
            SearchResultItem(
                data = data(it),
                selectable = false,
                selected = false,
                onClickListener = { sticker, _ ->
                    openDetailScreen(
                        navController = navController,
                        stickerUuid = sticker.sticker.uuid
                    )
                }
            )
        }
    }
}