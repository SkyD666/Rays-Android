package com.skyd.rays.ui.screen.stickerslist

import android.os.Bundle
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.skyd.rays.R
import com.skyd.rays.base.mvi.getDispatcher
import com.skyd.rays.ext.navigate
import com.skyd.rays.ext.plus
import com.skyd.rays.ui.component.RaysIconButton
import com.skyd.rays.ui.component.RaysTopBar
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

    viewModel.getDispatcher(startWith = StickersListIntent.GetStickersList(query))

    Scaffold(
        topBar = {
            RaysTopBar(
                scrollBehavior = scrollBehavior,
                title = { Text(text = query.ifBlank { stringResource(R.string.stickers_list_screen_name) }) },
                actions = {
                    RaysIconButton(
                        enabled = uiState.listState is ListState.Success,
                        onClick = {
                            openExportFilesScreen(
                                navController = navController,
                                exportStickers = (uiState.listState as? ListState.Success)
                                    ?.stickerWithTagsList?.map { it.sticker.uuid },
                            )
                        },
                        imageVector = Icons.Default.FolderZip,
                        contentDescription = stringResource(id = R.string.stickers_list_screen_export_current_stickers),
                    )
                }
            )
        }
    ) { paddingValues ->
        when (val listState = uiState.listState) {
            ListState.Init -> Unit
            is ListState.Success -> {
                LazyVerticalStaggeredGrid(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = paddingValues + PaddingValues(16.dp),
                    columns = StaggeredGridCells.Adaptive(125.dp),
                    verticalItemSpacing = 12.dp,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(items = listState.stickerWithTagsList, key = { it.sticker.uuid }) {
                        SearchResultItem(
                            data = it,
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
        }
    }
}