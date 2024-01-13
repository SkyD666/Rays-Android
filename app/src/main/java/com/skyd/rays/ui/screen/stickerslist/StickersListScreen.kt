package com.skyd.rays.ui.screen.stickerslist

import android.os.Bundle
import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.skyd.rays.R
import com.skyd.rays.appContext
import com.skyd.rays.base.mvi.getDispatcher
import com.skyd.rays.ext.dataStore
import com.skyd.rays.ext.getOrDefault
import com.skyd.rays.ext.navigate
import com.skyd.rays.ext.onScaleEvent
import com.skyd.rays.ext.plus
import com.skyd.rays.model.preference.StickerItemWidthPreference
import com.skyd.rays.ui.component.RaysIconButton
import com.skyd.rays.ui.component.RaysTopBar
import com.skyd.rays.ui.local.LocalNavController
import com.skyd.rays.ui.local.LocalStickerItemWidth
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
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val navController = LocalNavController.current
    val uiState by viewModel.viewState.collectAsStateWithLifecycle()
    val clipboardManager: ClipboardManager = LocalClipboardManager.current

    viewModel.getDispatcher(startWith = StickersListIntent.GetStickersList(query))

    Scaffold(
        topBar = {
            RaysTopBar(
                scrollBehavior = scrollBehavior,
                title = {
                    Text(
                        text = query.ifBlank { stringResource(R.string.stickers_list_screen_name) },
                        modifier = Modifier.run {
                            if (query.isBlank()) this
                            else clickable { clipboardManager.setText(AnnotatedString(query)) }
                        }
                    )
                },
                actions = {
                    RaysIconButton(
                        enabled = !(uiState.listState as? ListState.Success)
                            ?.stickerWithTagsList.isNullOrEmpty(),
                        onClick = {
                            openExportFilesScreen(
                                navController = navController,
                                exportStickers = (uiState.listState as? ListState.Success)
                                    ?.stickerWithTagsList?.map { it.sticker.uuid }.orEmpty(),
                            )
                        },
                        imageVector = Icons.Default.FolderZip,
                        contentDescription = stringResource(id = R.string.stickers_list_screen_export_current_stickers),
                    )
                }
            )
        }
    ) { paddingValues ->
        var stickerItemWidth by rememberSaveable{
            mutableStateOf(context.dataStore.getOrDefault(StickerItemWidthPreference))
        }

        when (val listState = uiState.listState) {
            ListState.Init -> Unit
            is ListState.Success -> {
                LazyVerticalStaggeredGrid(
                    modifier = Modifier
                        .fillMaxSize()
                        .onScaleEvent(
                            onFingerCountChange = { },
                            onScale = {
                                stickerItemWidth = (stickerItemWidth.dp * ((it - 1) * 0.9f + 1))
                                    .coerceIn(StickerItemWidthPreference.range).value
                                StickerItemWidthPreference.put(
                                    context = context,
                                    scope = scope,
                                    value = stickerItemWidth,
                                )
                            }
                        ),
                    contentPadding = paddingValues + PaddingValues(16.dp),
                    columns = StaggeredGridCells.Adaptive(stickerItemWidth.dp),
                    verticalItemSpacing = 12.dp,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(items = listState.stickerWithTagsList, key = { it.sticker.uuid }) {
                        SearchResultItem(
                            data = it,
                            selectable = false,
                            selected = false,
                            showTitle = stickerItemWidth.dp >= 111.dp,
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
        }
    }
}