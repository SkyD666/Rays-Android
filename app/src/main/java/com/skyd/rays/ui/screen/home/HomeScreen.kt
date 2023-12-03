package com.skyd.rays.ui.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.staggeredgrid.LazyHorizontalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skyd.rays.R
import com.skyd.rays.base.mvi.getDispatcher
import com.skyd.rays.ext.isCompact
import com.skyd.rays.ui.component.AnimatedPlaceholder
import com.skyd.rays.ui.component.RaysExtendedFloatingActionButton
import com.skyd.rays.ui.component.RaysFloatingActionButton
import com.skyd.rays.ui.component.RaysImage
import com.skyd.rays.ui.component.dialog.WaitingDialog
import com.skyd.rays.ui.local.LocalNavController
import com.skyd.rays.ui.local.LocalWindowSizeClass
import com.skyd.rays.ui.screen.add.openAddScreen
import com.skyd.rays.ui.screen.detail.openDetailScreen
import com.skyd.rays.ui.screen.search.SEARCH_SCREEN_ROUTE
import com.skyd.rays.ui.screen.stickerslist.openStickersListScreen


const val HOME_SCREEN_ROUTE = "homeScreen"

@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val navController = LocalNavController.current
    val snackbarHostState = remember { SnackbarHostState() }
    val uiState by viewModel.viewState.collectAsStateWithLifecycle()
    var fabHeight by remember { mutableStateOf(0.dp) }

    val dispatch = viewModel.getDispatcher(startWith = HomeIntent.GetHomeList)

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            HomeScreenFloatingActionButton(
                onSizeWithSinglePaddingChanged = { _, height -> fabHeight = height }
            )
        }
    ) { innerPaddings ->
        Column(
            modifier = Modifier
                .padding(
                    start = innerPaddings.calculateStartPadding(LocalLayoutDirection.current),
                    end = innerPaddings.calculateEndPadding(LocalLayoutDirection.current),
                    bottom = innerPaddings.calculateBottomPadding(),
                )
                .fillMaxSize()
                .padding(top = innerPaddings.calculateTopPadding()),
        ) {
            FilledTonalButton(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .height(56.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                onClick = { navController.navigate(SEARCH_SCREEN_ROUTE) }
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    tint = MaterialTheme.colorScheme.onSurface,
                    contentDescription = null
                )
                Text(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    text = stringResource(R.string.home_screen_search_hint),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            when (val homeUiState = uiState.homeListState) {
                is HomeListState.Init -> {
                    HomeEmptyPlaceholder()
                }

                is HomeListState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 16.dp + fabHeight)
                    ) {
                        item {
                            val mostSharedStickersList = homeUiState.mostSharedStickersList
                            DisplayStickersRow(
                                title = stringResource(id = R.string.home_screen_most_shared_stickers),
                                count = mostSharedStickersList.size,
                                itemImage = { mostSharedStickersList[it].sticker.uuid },
                                itemTitle = { mostSharedStickersList[it].sticker.title },
                                onItemClick = {
                                    openDetailScreen(
                                        navController = navController,
                                        stickerUuid = mostSharedStickersList[it].sticker.uuid
                                    )
                                },
                            )
                        }
                        item {
                            val recentCreatedStickersList = homeUiState.recentCreatedStickersList
                            DisplayStickersRow(
                                title = stringResource(id = R.string.home_screen_recent_create_stickers),
                                count = recentCreatedStickersList.size,
                                itemImage = { recentCreatedStickersList[it].sticker.uuid },
                                itemTitle = { recentCreatedStickersList[it].sticker.title },
                                onItemClick = {
                                    openDetailScreen(
                                        navController = navController,
                                        stickerUuid = recentCreatedStickersList[it].sticker.uuid
                                    )
                                },
                            )
                        }
//                        item {
//                            val recommendTagsList = homeUiState.recommendTagsList
//                            DisplayTagsRow(
//                                title = stringResource(id = R.string.home_screen_recommend_tags),
//                                count = recommendTagsList.size,
//                                itemImage = { recommendTagsList[it].stickerUuid },
//                                itemTitle = { recommendTagsList[it].tag },
//                                onItemClick = {
//                                    openStickersListScreen(
//                                        navController = navController,
//                                        query = recommendTagsList[it].tag,
//                                    )
//                                },
//                            )
//                        }

                        item {
                            val randomTagsList = homeUiState.randomTagsList
                            DisplayTagsRow(
                                title = stringResource(id = R.string.home_screen_random_tags),
                                count = randomTagsList.size,
                                itemImage = { randomTagsList[it].stickerUuid },
                                itemTitle = { randomTagsList[it].tag },
                                onItemClick = {
                                    openStickersListScreen(
                                        navController = navController,
                                        query = randomTagsList[it].tag,
                                    )
                                },
                            )
                        }
                    }
                }
            }
        }

        WaitingDialog(visible = uiState.loadingDialog)
    }
}

@Composable
private fun DisplayStickersRow(
    title: String,
    count: Int,
    itemImage: (Int) -> String,
    itemTitle: (Int) -> String,
    onItemClick: (Int) -> Unit,
) {
    Column {
        Text(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            text = title,
            style = MaterialTheme.typography.headlineSmall,
        )
        if (count == 0) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp),
                textAlign = TextAlign.Center,
                text = stringResource(id = R.string.empty_tip)
            )
        }
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(count) { index ->
                Column(modifier = Modifier.width(IntrinsicSize.Min)) {
                    ElevatedCard(onClick = { onItemClick(index) }) {
                        RaysImage(
                            modifier = Modifier
                                .height(150.dp)
                                .aspectRatio(1f),
                            uuid = itemImage(index),
                            contentScale = ContentScale.Crop,
                        )
                    }
                    Text(
                        modifier = Modifier.padding(top = 6.dp),
                        text = itemTitle(index),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(5.dp))
    }
}


@Composable
private fun DisplayTagsRow(
    title: String,
    count: Int,
    itemImage: (Int) -> String,
    itemTitle: (Int) -> String,
    onItemClick: (Int) -> Unit,
) {
    Column {
        Text(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            text = title,
            style = MaterialTheme.typography.headlineSmall,
        )
        if (count == 0) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp),
                textAlign = TextAlign.Center,
                text = stringResource(id = R.string.empty_tip)
            )
        }
        LazyHorizontalStaggeredGrid(
            modifier = Modifier.height(160.dp),
            rows = StaggeredGridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalItemSpacing = 8.dp,
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(count) { index ->
                ElevatedCard(
                    modifier = Modifier.aspectRatio(2f),
                    onClick = { onItemClick(index) }
                ) {
                    Box {
                        RaysImage(
                            modifier = Modifier.fillMaxSize(),
                            uuid = itemImage(index),
                            contentScale = ContentScale.Crop,
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.2f))
                        ) {
                            Text(
                                modifier = Modifier
                                    .padding(6.dp)
                                    .align(Alignment.Center),
                                text = itemTitle(index),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.labelLarge,
                                textAlign = TextAlign.Center,
                                color = Color.White,
                            )
                        }

                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(5.dp))
    }
}

@Composable
private fun HomeScreenFloatingActionButton(
    onSizeWithSinglePaddingChanged: ((width: Dp, height: Dp) -> Unit)
) {
    val navController = LocalNavController.current
    val windowSizeClass = LocalWindowSizeClass.current

    val content: @Composable () -> Unit = remember {
        @Composable {
            Icon(imageVector = Icons.Default.Add, contentDescription = null)
        }
    }
    val onClick = remember {
        {
            openAddScreen(
                navController = navController,
                stickers = mutableListOf(),
                isEdit = false,
            )
        }
    }

    if (windowSizeClass.isCompact) {
        RaysFloatingActionButton(
            content = { content() },
            onClick = onClick,
            onSizeWithSinglePaddingChanged = onSizeWithSinglePaddingChanged,
            contentDescription = stringResource(R.string.home_screen_add),
        )
    } else {
        RaysExtendedFloatingActionButton(
            text = { Text(text = stringResource(R.string.home_screen_add)) },
            icon = content,
            onClick = onClick,
            onSizeWithSinglePaddingChanged = onSizeWithSinglePaddingChanged,
            contentDescription = stringResource(R.string.home_screen_add),
        )
    }
}

@Composable
fun HomeEmptyPlaceholder() {
    AnimatedPlaceholder(
        resId = R.raw.lottie_genshin_impact_keqing_1,
        tip = stringResource(id = R.string.home_screen_empty_tip)
    )
}
