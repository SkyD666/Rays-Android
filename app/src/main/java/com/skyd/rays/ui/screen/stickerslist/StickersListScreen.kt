package com.skyd.rays.ui.screen.stickerslist

import android.os.Bundle
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.skyd.rays.R
import com.skyd.rays.ext.isCompact
import com.skyd.rays.ext.navigate
import com.skyd.rays.ext.plus
import com.skyd.rays.ext.startWith
import com.skyd.rays.ui.component.RaysTopBar
import com.skyd.rays.ui.local.LocalNavController
import com.skyd.rays.ui.local.LocalWindowSizeClass
import com.skyd.rays.ui.screen.detail.openDetailScreen
import com.skyd.rays.ui.screen.home.searchbar.SearchResultItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext

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
    val windowSizeClass = LocalWindowSizeClass.current

    val intentChannel = remember { Channel<StickersListIntent>(Channel.UNLIMITED) }
    LaunchedEffect(Unit) {
        withContext(Dispatchers.Main.immediate) {
            intentChannel
                .consumeAsFlow()
                .startWith(StickersListIntent.RefreshStickersList(query))
                .onEach(viewModel::processIntent)
                .collect()
        }
    }
    val dispatch = remember {
        { intent: StickersListIntent ->
            intentChannel.trySend(intent).getOrThrow()
        }
    }

    LaunchedEffect(query) {
        dispatch(StickersListIntent.RefreshStickersList(query))
    }

    Scaffold(
        topBar = {
            RaysTopBar(
                scrollBehavior = scrollBehavior,
                title = { Text(text = stringResource(R.string.stickers_list_screen_name)) },
            )
        }
    ) { paddingValues ->
        when (val listState = uiState.listState) {
            ListState.Init -> Unit
            is ListState.Success -> {
                LazyVerticalStaggeredGrid(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = paddingValues +
                            PaddingValues(horizontal = 16.dp) +
                            PaddingValues(bottom = 16.dp),
                    columns = StaggeredGridCells.Fixed(if (windowSizeClass.isCompact) 2 else 4),
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