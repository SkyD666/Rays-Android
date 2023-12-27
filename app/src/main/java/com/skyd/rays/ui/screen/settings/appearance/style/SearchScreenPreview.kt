package com.skyd.rays.ui.screen.settings.appearance.style

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.skyd.rays.R
import com.skyd.rays.ext.popBackStackWithLifecycle
import com.skyd.rays.model.bean.StickerBean
import com.skyd.rays.model.bean.StickerWithTags
import com.skyd.rays.model.bean.TagBean
import com.skyd.rays.ui.component.RaysIconButton
import com.skyd.rays.ui.local.LocalCurrentStickerUuid
import com.skyd.rays.ui.local.LocalNavController
import com.skyd.rays.ui.local.LocalShowLastQuery
import com.skyd.rays.ui.local.LocalShowPopularTags
import com.skyd.rays.ui.screen.search.PopularTagsBar
import com.skyd.rays.ui.screen.search.SearchResultList
import com.skyd.rays.ui.screen.search.TrailingIcon

@Composable
fun SearchScreenPreview() {
    RaysSearchBarPreview()
}

@Composable
private fun RaysSearchBarPreview() {
    val navController = LocalNavController.current
    val currentStickerUuid = LocalCurrentStickerUuid.current
    val searchResultListState = rememberLazyStaggeredGridState()

    Box(
        Modifier
            .semantics { isTraversalGroup = true }
            .zIndex(1f)
            .fillMaxWidth()
    ) {
        Box(modifier = Modifier.align(Alignment.TopCenter)) {
            val query = if (LocalShowLastQuery.current) "LOL" else ""
            SearchBar(
                windowInsets = WindowInsets(0.dp),
                onQueryChange = {},
                query = query,
                onSearch = { },
                active = true,
                onActiveChange = { if (!it) navController.popBackStackWithLifecycle() },
                placeholder = { Text(text = stringResource(R.string.home_screen_search_hint)) },
                leadingIcon = {
                    RaysIconButton(
                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = stringResource(id = R.string.home_screen_close_search),
                        onClick = { }
                    )
                },
                trailingIcon = { TrailingIcon(showClearButton = query.isNotBlank()) {} },
            ) {
                AnimatedVisibility(visible = LocalShowPopularTags.current) {
                    PopularTagsBar(
                        onTagClicked = {},
                        tags = listOf("Tag" to 1f, "LOL" to 0.9f),
                    )
                }
                SearchResultList(
                    state = searchResultListState,
                    contentPadding = PaddingValues(horizontal = 16.dp),
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
                    onItemClickListener = { _, _ -> },
                    multiSelect = false,
                    onMultiSelectChanged = {},
                    onInvertSelectClick = {},
                    selectedStickers = emptyList()
                )
            }
        }
    }
}

