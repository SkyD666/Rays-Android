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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.ArrowDropUp
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
import com.skyd.rays.model.bean.StickerWithTags
import com.skyd.rays.model.preference.privacy.rememberShouldBlur
import com.skyd.rays.model.preference.search.SearchResultReversePreference
import com.skyd.rays.ui.component.EmptyPlaceholder
import com.skyd.rays.ui.component.RaysImage
import com.skyd.rays.ui.component.ScalableLazyVerticalStaggeredGrid
import com.skyd.rays.ui.local.LocalSearchResultReverse
import com.skyd.rays.ui.local.LocalStickerItemWidth
import com.skyd.rays.util.sendStickerByUuid

@Composable
fun SearchResultList(
    state: LazyStaggeredGridState,
    contentPadding: PaddingValues,
    dataList: List<StickerWithTags>,
    onSelectChanged: ((data: StickerWithTags, selected: Boolean) -> Unit)? = null,
    onClick: ((data: StickerWithTags) -> Unit)? = null,
    multiSelect: Boolean,
    onMultiSelectChanged: (Boolean) -> Unit,
    onInvertSelectClick: () -> Unit,
    selectedStickers: Collection<String>,
) {
    Column {
        SearchResultConfigBar(
            stickersCount = dataList.size,
            multiSelect = multiSelect,
            onMultiSelectChanged = onMultiSelectChanged,
            onInvertSelectClick = onInvertSelectClick,
        )
        if (dataList.isEmpty()) {
            EmptyPlaceholder()
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
                        selected = selectedStickers.contains(it.sticker.uuid),
                        onSelectChanged = onSelectChanged,
                        onClick = onClick,
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
                            imageVector = if (expandMenu) Icons.Outlined.ArrowDropUp
                            else Icons.Outlined.ArrowDropDown,
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
                label = { Text(text = stringResource(R.string.multi_select)) },
            )
        }

        item {
            if (multiSelect) {
                SuggestionChip(
                    onClick = onInvertSelectClick,
                    label = { Text(text = stringResource(R.string.invert_selection)) },
                )
            }
        }
    }
}

@Composable
fun SearchResultItem(
    modifier: Modifier = Modifier,
    data: StickerWithTags,
    selectable: Boolean = false,
    selected: Boolean = false,
    showTitle: Boolean = LocalStickerItemWidth.current.dp >= 111.dp,
    contentScale: ContentScale = ContentScale.Crop,
    imageAspectRatio: Float? = 0.97f,
    onSelectChanged: ((data: StickerWithTags, selected: Boolean) -> Unit)? = null,
    onClick: ((data: StickerWithTags) -> Unit)? = null,
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
                onClick = {
                    if (selectable) onSelectChanged?.invoke(data, !selected)
                    else onClick?.invoke(data)
                }
            ),
    ) {
        Box {
            RaysImage(
                modifier = Modifier
                    .fillMaxWidth()
                    .run { imageAspectRatio?.let { aspectRatio(it) } ?: this },
                blur = rememberShouldBlur(c = data.tags.map { it.tag } + data.sticker.title),
                contentScale = contentScale,
                uuid = data.sticker.uuid
            )
            if (selected) {
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

@Composable
fun SearchResultItemPlaceholder() {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) { }
}