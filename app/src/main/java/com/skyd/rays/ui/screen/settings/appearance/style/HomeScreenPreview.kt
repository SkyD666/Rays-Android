package com.skyd.rays.ui.screen.settings.appearance.style

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.skyd.rays.R
import com.skyd.rays.model.bean.StickerBean
import com.skyd.rays.model.bean.StickerWithTags
import com.skyd.rays.model.bean.TagBean
import com.skyd.rays.ui.component.RaysIconButton
import com.skyd.rays.ui.local.LocalCurrentStickerUuid
import com.skyd.rays.ui.local.LocalShowPopularTags
import com.skyd.rays.ui.screen.home.HomeEmptyPlaceholder
import com.skyd.rays.ui.screen.home.MainCard
import com.skyd.rays.ui.screen.home.searchbar.HomeMenu
import com.skyd.rays.ui.screen.home.searchbar.PopularTagsBar
import com.skyd.rays.ui.screen.home.searchbar.SearchResultList
import com.skyd.rays.ui.screen.home.searchbar.TrailingIcon

@Composable
fun HomeScreenPreview() {
    val currentStickerUuid = LocalCurrentStickerUuid.current
    Column(modifier = Modifier.fillMaxWidth()) {
        RaysSearchBarPreview()
        Spacer(modifier = Modifier.height(16.dp))

        if (currentStickerUuid.isNotBlank()) {
            MainCard(
                stickerWithTags = StickerWithTags(
                    sticker = StickerBean(title = "").apply {
                        uuid = LocalCurrentStickerUuid.current
                    },
                    tags = listOf(TagBean(tag = "Tag"), TagBean(tag = "LOL"))
                )
            )
        } else {
            HomeEmptyPlaceholder()
        }
    }
}

@Composable
private fun RaysSearchBarPreview() {
    var menuExpanded by rememberSaveable { mutableStateOf(false) }
    var active by rememberSaveable { mutableStateOf(false) }
    val searchBarHorizontalPadding: Dp by animateDpAsState(
        if (active) 0.dp else 16.dp,
        label = "searchBarHorizontalPadding"
    )
    val currentStickerUuid = LocalCurrentStickerUuid.current
    val searchResultListState = rememberLazyStaggeredGridState()

    Box(
        Modifier
            .semantics { isTraversalGroup = true }
            .zIndex(1f)
            .fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(horizontal = searchBarHorizontalPadding)
        ) {
            SearchBar(
                windowInsets = WindowInsets(0.dp),
                onQueryChange = {},
                query = "LOL",
                onSearch = { },
                active = active,
                onActiveChange = {
                    active = it
                },
                placeholder = { Text(text = stringResource(R.string.home_screen_search_hint)) },
                leadingIcon = {
                    if (active) {
                        RaysIconButton(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = stringResource(id = R.string.home_screen_close_search),
                            onClick = { active = false }
                        )
                    } else {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null)
                    }
                },
                trailingIcon = {
                    if (active) {
                        TrailingIcon(showClearButton = true) {}
                    } else {
                        RaysIconButton(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = stringResource(id = R.string.home_screen_open_menu),
                            onClick = { menuExpanded = true }
                        )
                        HomeMenu(
                            expanded = menuExpanded,
                            stickerMenuItemEnabled = currentStickerUuid.isNotBlank(),
                            onDismissRequest = { menuExpanded = false },
                            onDeleteClick = {},
                            onExportClick = {},
                            onCopyClick = {},
                            onStickerInfoClick = {},
                            onClearScreen = {},
                        )
                    }
                },
            ) {
                AnimatedVisibility(visible = LocalShowPopularTags.current) {
                    PopularTagsBar(
                        onTagClicked = {},
                        tags = listOf("Tag" to 1f, "LOL" to 0.9f),
                    )
                }
                SearchResultList(
                    state = searchResultListState,
                    dataList = if (currentStickerUuid.isBlank()) {
                        emptyList()
                    } else {
                        listOf(
                            StickerWithTags(
                                sticker = StickerBean(title = "").apply {
                                    uuid = currentStickerUuid
                                },
                                tags = listOf(TagBean(tag = "轻音少女"), TagBean(tag = "揍你"))
                            )
                        )
                    },
                    onItemClickListener = { _, _ -> active = false },
                    multiSelect = false,
                    onMultiSelectChanged = {},
                    onInvertSelectClick = {},
                    selectedStickers = emptyList()
                )
            }
        }
    }
}
