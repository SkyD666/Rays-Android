package com.skyd.rays.ui.screen.home

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.staggeredgrid.LazyHorizontalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Reply
import androidx.compose.material.icons.automirrored.outlined.ScheduleSend
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ImageSearch
import androidx.compose.material.icons.outlined.MoreTime
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Shuffle
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skyd.compone.component.ComponeExtendedFloatingActionButton
import com.skyd.compone.component.ComponeFloatingActionButton
import com.skyd.compone.component.ComponeIconButton
import com.skyd.compone.component.dialog.WaitingDialog
import com.skyd.compone.ext.minus
import com.skyd.compone.ext.plus
import com.skyd.compone.local.LocalNavController
import com.skyd.rays.R
import com.skyd.rays.base.mvi.getDispatcher
import com.skyd.rays.ext.isCompact
import com.skyd.rays.model.preference.privacy.shouldBlur
import com.skyd.rays.ui.component.EmptyPlaceholder
import com.skyd.rays.ui.component.RaysImage
import com.skyd.rays.ui.local.LocalWindowSizeClass
import com.skyd.rays.ui.screen.add.AddRoute
import com.skyd.rays.ui.screen.detail.DetailRoute
import com.skyd.rays.ui.screen.search.SearchRoute
import com.skyd.rays.ui.screen.search.imagesearch.ImageSearchRoute
import com.skyd.rays.ui.screen.stickerslist.StickersListRoute
import com.skyd.rays.util.sendStickerByUuid
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel


@Serializable
data object HomeRoute

@Composable
fun HomeScreen(viewModel: HomeViewModel = koinViewModel()) {
    val navController = LocalNavController.current
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val uiState by viewModel.viewState.collectAsStateWithLifecycle()
    var fabHeight by remember { mutableStateOf(0.dp) }

    viewModel.getDispatcher(startWith = HomeIntent.GetHomeList)

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
                onClick = { navController.navigate(SearchRoute) },
                contentPadding = ButtonDefaults.ContentPadding - PaddingValues(
                    end = ButtonDefaults.ContentPadding.calculateEndPadding(
                        LocalLayoutDirection.current
                    )
                ) + PaddingValues(end = 10.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Search,
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
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                ComponeIconButton(
                    onClick = { navController.navigate(ImageSearchRoute(baseImage = null)) },
                    imageVector = Icons.Outlined.ImageSearch,
                    tint = MaterialTheme.colorScheme.onSurface,
                    contentDescription = stringResource(R.string.image_search_screen_name)
                )
            }
            when (val homeUiState = uiState.homeListState) {
                is HomeListState.Init -> EmptyPlaceholder()
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
                                emptyIcon = Icons.AutoMirrored.Outlined.Reply,
                                key = { mostSharedStickersList[it].sticker.uuid },
                                itemImage = { mostSharedStickersList[it].sticker.uuid },
                                itemTitle = { mostSharedStickersList[it].sticker.title },
                                onItemLongClick = {
                                    context.sendStickerByUuid(
                                        uuid = mostSharedStickersList[it].sticker.uuid,
                                        onSuccess = { mostSharedStickersList[it].sticker.shareCount++ }
                                    )
                                },
                                onItemClick = {
                                    navController.navigate(
                                        DetailRoute(stickerUuid = mostSharedStickersList[it].sticker.uuid)
                                    )
                                },
                                itemShouldBlur = { index ->
                                    shouldBlur(
                                        context = context,
                                        c = mostSharedStickersList[index].tags.map { it.tag } +
                                                mostSharedStickersList[index].sticker.title,
                                    )
                                },
                            )
                        }
                        item {
                            val recentSharedStickersList = homeUiState.recentSharedStickersList
                            DisplayStickersRow(
                                title = stringResource(id = R.string.home_screen_recent_shared_stickers),
                                count = recentSharedStickersList.size,
                                emptyIcon = Icons.AutoMirrored.Outlined.ScheduleSend,
                                key = { recentSharedStickersList[it].sticker.uuid },
                                itemImage = { recentSharedStickersList[it].sticker.uuid },
                                itemTitle = { recentSharedStickersList[it].sticker.title },
                                onItemLongClick = {
                                    context.sendStickerByUuid(
                                        uuid = recentSharedStickersList[it].sticker.uuid,
                                        onSuccess = { recentSharedStickersList[it].sticker.shareCount++ }
                                    )
                                },
                                onItemClick = {
                                    navController.navigate(
                                        DetailRoute(stickerUuid = recentSharedStickersList[it].sticker.uuid)
                                    )
                                },
                                itemShouldBlur = { index ->
                                    shouldBlur(
                                        context = context,
                                        c = recentSharedStickersList[index].tags.map { it.tag } +
                                                recentSharedStickersList[index].sticker.title,
                                    )
                                },
                            )
                        }
                        item {
                            val recentCreatedStickersList = homeUiState.recentCreatedStickersList
                            DisplayStickersRow(
                                title = stringResource(id = R.string.home_screen_recent_create_stickers),
                                count = recentCreatedStickersList.size,
                                emptyIcon = Icons.Outlined.MoreTime,
                                key = { recentCreatedStickersList[it].sticker.uuid },
                                itemImage = { recentCreatedStickersList[it].sticker.uuid },
                                itemTitle = { recentCreatedStickersList[it].sticker.title },
                                onItemLongClick = {
                                    context.sendStickerByUuid(
                                        uuid = recentCreatedStickersList[it].sticker.uuid,
                                        onSuccess = { recentCreatedStickersList[it].sticker.shareCount++ }
                                    )
                                },
                                onItemClick = {
                                    navController.navigate(
                                        DetailRoute(stickerUuid = recentCreatedStickersList[it].sticker.uuid)
                                    )
                                },
                                itemShouldBlur = { index ->
                                    shouldBlur(
                                        context = context,
                                        c = recentCreatedStickersList[index].tags.map { it.tag } +
                                                recentCreatedStickersList[index].sticker.title,
                                    )
                                },
                            )
                        }
//                        item {
//                            val recommendTagsList = homeUiState.recommendTagsList
//                            DisplayTagsRow(
//                                title = stringResource(id = R.string.home_screen_recommend_tags),
//                                count = recommendTagsList.size,
//                                key = {},
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
                                key = { randomTagsList[it].tag },
                                emptyIcon = Icons.Outlined.Shuffle,
                                itemImage = { randomTagsList[it].stickerUuid },
                                itemTitle = { randomTagsList[it].tag },
                                onItemClick = {
                                    navController.navigate(
                                        StickersListRoute(query = randomTagsList[it].tag)
                                    )
                                },
                                itemShouldBlur = { index ->
                                    shouldBlur(
                                        context = context,
                                        c = listOf(randomTagsList[index].tag)
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
    emptyIcon: ImageVector,
    key: ((index: Int) -> Any)? = null,
    itemImage: (Int) -> String,
    itemTitle: (Int) -> String,
    onItemLongClick: (Int) -> Unit,
    onItemClick: (Int) -> Unit,
    itemShouldBlur: (Int) -> Boolean,
) {
    Column {
        Text(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            text = title,
            style = MaterialTheme.typography.headlineSmall,
        )
        if (count == 0) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp, horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    modifier = Modifier.size(40.dp),
                    imageVector = emptyIcon,
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    textAlign = TextAlign.Center,
                    text = stringResource(id = R.string.empty_tip),
                )
            }
        } else {
            LazyRow(
                modifier = Modifier.animateContentSize(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(count = count, key = key) { index ->
                    Column(modifier = Modifier.width(IntrinsicSize.Min)) {
                        ElevatedCard {
                            RaysImage(
                                modifier = Modifier
                                    .height(150.dp)
                                    .aspectRatio(1f)
                                    .combinedClickable(
                                        onLongClick = { onItemLongClick(index) },
                                        onClick = { onItemClick(index) },
                                    ),
                                uuid = itemImage(index),
                                blur = itemShouldBlur(index),
                                contentScale = ContentScale.Crop,
                            )
                        }
                        if (itemTitle(index).isNotBlank()) {
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
            }
        }
        Spacer(modifier = Modifier.height(5.dp))
    }
}


@Composable
private fun DisplayTagsRow(
    title: String,
    count: Int,
    emptyIcon: ImageVector,
    key: ((index: Int) -> Any)? = null,
    itemImage: (Int) -> String,
    itemTitle: (Int) -> String,
    onItemClick: (Int) -> Unit,
    itemShouldBlur: (Int) -> Boolean,
) {
    Column {
        Text(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            text = title,
            style = MaterialTheme.typography.headlineSmall,
        )
        if (count == 0) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp, horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    modifier = Modifier.size(40.dp),
                    imageVector = emptyIcon,
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    textAlign = TextAlign.Center,
                    text = stringResource(id = R.string.empty_tip),
                )
            }
        } else {
            LazyHorizontalStaggeredGrid(
                modifier = Modifier.height(160.dp),
                rows = StaggeredGridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalItemSpacing = 8.dp,
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(count = count, key = key) { index ->
                    ElevatedCard(
                        modifier = Modifier.aspectRatio(2f),
                        onClick = { onItemClick(index) }
                    ) {
                        Box {
                            RaysImage(
                                modifier = Modifier.fillMaxSize(),
                                uuid = itemImage(index),
                                blur = itemShouldBlur(index),
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
            Icon(imageVector = Icons.Outlined.Add, contentDescription = null)
        }
    }
    val onClick = remember {
        {
            navController.navigate(AddRoute(stickers = emptyList(), isEdit = false))
        }
    }

    if (windowSizeClass.isCompact) {
        ComponeFloatingActionButton(
            content = { content() },
            onClick = onClick,
            onSizeWithSinglePaddingChanged = onSizeWithSinglePaddingChanged,
            contentDescription = stringResource(R.string.home_screen_add),
        )
    } else {
        ComponeExtendedFloatingActionButton(
            text = { Text(text = stringResource(R.string.home_screen_add)) },
            icon = content,
            onClick = onClick,
            onSizeWithSinglePaddingChanged = onSizeWithSinglePaddingChanged,
            contentDescription = stringResource(R.string.home_screen_add),
        )
    }
}
