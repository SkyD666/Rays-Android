package com.skyd.rays.ui.screen.search

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Badge
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.skyd.rays.R
import com.skyd.rays.ext.isCompact
import com.skyd.rays.model.bean.StickerWithTags
import com.skyd.rays.model.preference.privacy.rememberShouldBlur
import com.skyd.rays.model.preference.search.SearchResultReversePreference
import com.skyd.rays.ui.component.AnimatedPlaceholder
import com.skyd.rays.ui.component.RaysIconButton
import com.skyd.rays.ui.component.RaysImage
import com.skyd.rays.ui.component.ScalableLazyVerticalStaggeredGrid
import com.skyd.rays.ui.component.dialog.WaitingDialog
import com.skyd.rays.ui.local.LocalSearchResultReverse
import com.skyd.rays.ui.local.LocalStickerItemWidth
import com.skyd.rays.ui.local.LocalWindowSizeClass
import com.skyd.rays.util.sendStickerByUuid
import com.skyd.rays.util.sendStickersByUuids

@Composable
fun SearchResultList(
    state: LazyStaggeredGridState,
    contentPadding: PaddingValues,
    dataList: List<StickerWithTags>,
    onItemClickListener: ((data: StickerWithTags, selected: Boolean) -> Unit)? = null,
    multiSelect: Boolean,
    onMultiSelectChanged: (Boolean) -> Unit,
    onInvertSelectClick: () -> Unit,
    selectedStickers: List<StickerWithTags>,
) {

    Column {
        SearchResultConfigBar(
            stickersCount = dataList.size,
            multiSelect = multiSelect,
            onMultiSelectChanged = onMultiSelectChanged,
            onInvertSelectClick = onInvertSelectClick,
        )
        if (dataList.isEmpty()) {
            AnimatedPlaceholder(
                resId = R.raw.lottie_genshin_impact_klee_2,
                tip = stringResource(id = R.string.search_screen_no_search_result_tip)
            )
        } else {
            ScalableLazyVerticalStaggeredGrid(
                modifier = Modifier.fillMaxSize(),
                state = state,
                contentPadding = contentPadding,
                verticalItemSpacing = 12.dp,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items = dataList, key = { it.sticker.uuid }) {
                    SearchResultItem(
                        data = it,
                        selectable = multiSelect,
                        selected = selectedStickers.contains(it),
                        onClickListener = onItemClickListener
                    )
                }
            }
        }
    }
}

@Composable
fun SearchResultConfigBar(
    stickersCount: Int,
    multiSelect: Boolean,
    onMultiSelectChanged: (Boolean) -> Unit,
    onInvertSelectClick: () -> Unit,
) {
    val searchResultReverse = LocalSearchResultReverse.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var expandMenu by rememberSaveable { mutableStateOf(false) }

    BackHandler(multiSelect) {
        onMultiSelectChanged(false)
    }

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        item {
            Badge {
                AnimatedContent(
                    targetState = stickersCount,
                    label = "badgeAnimatedContent",
                    transitionSpec = {
                        (fadeIn(animationSpec = tween(220, delayMillis = 90)) +
                                slideInVertically(initialOffsetY = { it }))
                            .togetherWith(
                                fadeOut(animationSpec = tween(90)) +
                                        slideOutVertically(targetOffsetY = { -it })
                            )
                    }
                ) { targetCount ->
                    Text(text = targetCount.toString())
                }
            }
        }

        item { VerticalDivider(modifier = Modifier.height(16.dp)) }

        item {
            Box {
                FilterChip(
                    selected = false,
                    onClick = { expandMenu = !expandMenu },
                    label = { Text(text = stringResource(R.string.search_result_sort)) },
                    trailingIcon = {
                        Icon(
                            imageVector = if (expandMenu) Icons.Default.ArrowDropUp
                            else Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier.size(AssistChipDefaults.IconSize),
                        )
                    }
                )
                SearchResultSortMenu(
                    expanded = expandMenu,
                    onDismissRequest = { expandMenu = false },
                )
            }
        }

        item {
            FilterChip(
                selected = searchResultReverse,
                onClick = {
                    SearchResultReversePreference.put(
                        context = context,
                        scope = scope,
                        value = !searchResultReverse
                    )
                },
                label = { Text(text = stringResource(R.string.search_result_reverse)) },
            )
        }

        item { VerticalDivider(modifier = Modifier.height(16.dp)) }

        item {
            FilterChip(
                selected = multiSelect,
                onClick = { onMultiSelectChanged(!multiSelect) },
                label = { Text(text = stringResource(R.string.search_result_multi_select)) },
            )
        }

        item {
            if (multiSelect) {
                SuggestionChip(
                    onClick = onInvertSelectClick,
                    label = { Text(text = stringResource(R.string.search_result_invert_selection)) },
                )
            }
        }
    }
}

@Composable
internal fun MultiSelectActionBar(
    modifier: Modifier = Modifier,
    selectedStickers: List<StickerWithTags>,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onExportClick: () -> Unit,
    onExportAsZipClick: () -> Unit,
) {
    val context = LocalContext.current
    val windowSizeClass = LocalWindowSizeClass.current
    var showWaitingDialog by rememberSaveable { mutableStateOf(false) }

    val items = remember {
        arrayOf<@Composable () -> Unit>(
            @Composable {
                RaysIconButton(
                    onClick = {
                        showWaitingDialog = true
                        context.sendStickersByUuids(
                            uuids = selectedStickers.map { it.sticker.uuid },
                            onSuccess = {
                                selectedStickers.forEach { it.sticker.shareCount++ }
                                showWaitingDialog = false
                            }
                        )
                    },
                    enabled = selectedStickers.isNotEmpty(),
                    imageVector = Icons.Default.Share,
                    contentDescription = stringResource(id = R.string.send_sticker)
                )
            },
            @Composable {
                RaysIconButton(
                    onClick = onEditClick,
                    enabled = selectedStickers.isNotEmpty(),
                    imageVector = Icons.Default.Edit,
                    contentDescription = stringResource(id = R.string.add_screen_name_edit)
                )
            },
            @Composable {
                RaysIconButton(
                    onClick = onExportClick,
                    enabled = selectedStickers.isNotEmpty(),
                    imageVector = Icons.Default.Save,
                    contentDescription = stringResource(id = R.string.home_screen_export)
                )
            },
            @Composable {
                RaysIconButton(
                    onClick = onExportAsZipClick,
                    enabled = selectedStickers.isNotEmpty(),
                    imageVector = Icons.Default.FolderZip,
                    contentDescription = stringResource(id = R.string.home_screen_export_to_backup_zip)
                )
            },
            @Composable {
                RaysIconButton(
                    onClick = onDeleteClick,
                    enabled = selectedStickers.isNotEmpty(),
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(id = R.string.home_screen_delete)
                )
            }
        )
    }
    if (windowSizeClass.isCompact) {
        Row(modifier = modifier.horizontalScroll(rememberScrollState())) {
            items.forEachIndexed { _, function -> function() }
        }
    } else {
        Column(modifier = modifier.verticalScroll(rememberScrollState())) {
            items.forEachIndexed { _, function -> function() }
        }
    }

    WaitingDialog(visible = showWaitingDialog)
}

@Composable
fun SearchResultItem(
    modifier: Modifier = Modifier,
    data: StickerWithTags,
    selectable: Boolean = false,
    selected: Boolean = false,
    showTitle: Boolean = LocalStickerItemWidth.current.dp >= 111.dp,
    onClickListener: ((data: StickerWithTags, selected: Boolean) -> Unit)? = null
) {
    val context = LocalContext.current
    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onLongClick = {
                    context.sendStickerByUuid(
                        uuid = data.sticker.uuid,
                        onSuccess = { data.sticker.shareCount++ }
                    )
                },
                onClick = { onClickListener?.invoke(data, !selected) }
            ),
    ) {
        Box {
            RaysImage(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.97f),
                blur = rememberShouldBlur(c = data.tags.map { it.tag } + data.sticker.title),
                contentScale = ContentScale.Crop,
                uuid = data.sticker.uuid
            )
            if (selectable && selected) {
                Icon(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = CircleShape
                        )
                        .padding(3.dp),
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = null
                )
            }
        }
        if (showTitle && data.sticker.title.isNotBlank()) {
            Text(
                modifier = Modifier
                    .padding(vertical = 10.dp, horizontal = 10.dp)
                    .basicMarquee(iterations = Int.MAX_VALUE),
                text = data.sticker.title,
                style = MaterialTheme.typography.titleMedium,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
        }
    }
}
