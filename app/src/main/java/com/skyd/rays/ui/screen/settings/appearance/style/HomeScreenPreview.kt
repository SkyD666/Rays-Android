package com.skyd.rays.ui.screen.settings.appearance.style

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ManageSearch
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.semantics.isContainer
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.skyd.rays.R
import com.skyd.rays.model.bean.StickerBean
import com.skyd.rays.model.bean.StickerWithTags
import com.skyd.rays.model.bean.TagBean
import com.skyd.rays.model.preference.StickerScalePreference
import com.skyd.rays.ui.component.AnimatedPlaceholder
import com.skyd.rays.ui.component.RaysIconButton
import com.skyd.rays.ui.component.RaysIconButtonStyle
import com.skyd.rays.ui.component.RaysImage
import com.skyd.rays.ui.local.LocalCurrentStickerUuid
import com.skyd.rays.ui.local.LocalHomeShareButtonAlignment
import com.skyd.rays.ui.local.LocalStickerScale
import com.skyd.rays.ui.screen.home.SearchResultList
import com.skyd.rays.ui.screen.home.TrailingIcon

@Composable
fun HomeScreenPreview() {
    val currentStickerUuid = LocalCurrentStickerUuid.current
    Column(modifier = Modifier.fillMaxWidth()) {
        RaysSearchBarPreview()
        Spacer(modifier = Modifier.height(16.dp))

        AnimatedVisibility(
            visible = currentStickerUuid.isNotBlank(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            MainCardPreview()
        }

        AnimatedVisibility(
            visible = currentStickerUuid.isBlank(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            AnimatedPlaceholder(
                resId = R.raw.lottie_genshin_impact_klee_1,
                tip = stringResource(id = R.string.home_screen_empty_tip)
            )
        }
    }
}

@Composable
private fun RaysSearchBarPreview() {
    var menuExpanded by rememberSaveable { mutableStateOf(false) }
    var active by rememberSaveable { mutableStateOf(false) }
    val searchBarHorizontalPadding: Dp by animateDpAsState(if (active) 0.dp else 16.dp)
    val currentStickerUuid = LocalCurrentStickerUuid.current
    val searchResultListState = rememberLazyStaggeredGridState()

    Box(
        Modifier
            .semantics { isContainer = true }
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
                query = "轻音少女",
                onSearch = { },
                active = active,
                onActiveChange = {
                    active = it
                },
                placeholder = { Text(text = stringResource(R.string.home_screen_search_hint)) },
                leadingIcon = {
                    if (active) {
                        RaysIconButton(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(id = R.string.home_screen_close_search),
                            onClick = { active = false }
                        )
                    } else {
                        RaysIconButton(
                            imageVector = Icons.Default.Menu,
                            contentDescription = stringResource(id = R.string.home_screen_open_menu),
                            onClick = { menuExpanded = true }
                        )
                    }
                },
                trailingIcon = {
                    if (active) {
                        TrailingIcon(showClearButton = true) { }
                    } else {
                        RaysIconButton(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(R.string.home_screen_add),
                            onClick = { }
                        )
                    }
                },
            ) {
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
                    onItemClickListener = { active = false }
                )
            }
            HomeMenuPreview(expanded = menuExpanded, onDismissRequest = { menuExpanded = false })
        }
    }
}

@Composable
private fun HomeMenuPreview(expanded: Boolean, onDismissRequest: () -> Unit) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
    ) {
        DropdownMenuItem(
            text = { Text(stringResource(R.string.home_screen_clear_current_sicker)) },
            onClick = onDismissRequest,
            leadingIcon = {
                Icon(
                    Icons.Default.Replay,
                    contentDescription = null
                )
            }
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.home_screen_edit)) },
            onClick = onDismissRequest,
            leadingIcon = {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = null
                )
            }
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.home_screen_delete)) },
            onClick = onDismissRequest,
            leadingIcon = {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null
                )
            }
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.home_screen_sticker_info)) },
            onClick = onDismissRequest,
            leadingIcon = {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null
                )
            }
        )
        Divider()
        DropdownMenuItem(
            text = { Text(stringResource(R.string.search_config_screen_name)) },
            onClick = onDismissRequest,
            leadingIcon = {
                Icon(
                    Icons.Default.ManageSearch,
                    contentDescription = null
                )
            }
        )
    }
}


@Composable
fun MainCardPreview() {
    val stickerWithTags = StickerWithTags(
        sticker = StickerBean(title = "").apply { uuid = LocalCurrentStickerUuid.current },
        tags = listOf(TagBean(tag = "轻音少女"), TagBean(tag = "揍你"))
    )
    val stickerBean = stickerWithTags.sticker
    val tags = stickerWithTags.tags

    Card(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp)
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .clickable { }
        ) {
            Box {
                RaysImage(
                    modifier = Modifier.fillMaxWidth(),
                    uuid = stickerWithTags.sticker.uuid,
                    contentScale = StickerScalePreference.toContentScale(LocalStickerScale.current),
                )
                Box(
                    modifier = Modifier.matchParentSize(),
                    contentAlignment = LocalHomeShareButtonAlignment.current
                ) {
                    RaysIconButton(
                        style = RaysIconButtonStyle.FilledTonal,
                        imageVector = Icons.Default.Share,
                        contentDescription = stringResource(R.string.home_screen_send_sticker),
                        onClick = { }
                    )
                }
            }
            if (stickerBean.title.isNotBlank()) {
                Text(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(top = 16.dp, bottom = if (tags.isEmpty()) 16.dp else 0.dp)
                        .basicMarquee(iterations = Int.MAX_VALUE),
                    text = stickerBean.title,
                    style = MaterialTheme.typography.titleLarge
                )
            }
            if (tags.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier
                        .padding(vertical = 6.dp, horizontal = 16.dp)
                        .fillMaxWidth()
                        .heightIn(max = 150.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    repeat(tags.size) { index ->
                        AssistChip(
                            onClick = { },
                            label = { Text(tags[index].tag) }
                        )
                    }
                }
            }
        }
    }
}
