package com.skyd.rays.ui.screen.settings.appearance.style

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ManageSearch
import androidx.compose.material.icons.automirrored.outlined.ManageSearch
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.skyd.rays.R
import com.skyd.rays.model.preference.ShowPopularTagsPreference
import com.skyd.rays.model.preference.search.ShowLastQueryPreference
import com.skyd.rays.ui.component.RaysTopBar
import com.skyd.rays.ui.component.RaysTopBarStyle
import com.skyd.rays.ui.component.SwitchSettingsItem
import com.skyd.rays.ui.local.LocalShowLastQuery
import com.skyd.rays.ui.local.LocalShowPopularTags

const val SEARCH_STYLE_SCREEN_ROUTE = "searchStyleScreen"

@Composable
fun SearchStyleScreen() {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            RaysTopBar(
                style = RaysTopBarStyle.Large,
                scrollBehavior = scrollBehavior,
                title = { Text(text = stringResource(R.string.search_style_screen_name)) },
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = paddingValues,
        ) {
            item {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .padding(vertical = 12.dp)
                            .heightIn(max = 300.dp)
                            .wrapContentHeight()
                            .fillMaxWidth(0.8f),
                    ) {
                        SearchScreenPreview()
                    }
                }
            }
            item {
                SwitchSettingsItem(
                    imageVector = Icons.Outlined.LocalOffer,
                    checked = LocalShowPopularTags.current,
                    text = stringResource(R.string.home_style_screen_show_popular_tags),
                    description = stringResource(R.string.home_style_screen_show_popular_tags_description),
                    onCheckedChange = {
                        ShowPopularTagsPreference.put(
                            context = context,
                            scope = scope,
                            value = it,
                        )
                    },
                )
            }
            item {
                SwitchSettingsItem(
                    imageVector = Icons.AutoMirrored.Outlined.ManageSearch,
                    checked = LocalShowLastQuery.current,
                    text = stringResource(R.string.home_style_screen_show_last_query),
                    description = stringResource(R.string.home_style_screen_show_last_query_description),
                    onCheckedChange = {
                        ShowLastQueryPreference.put(
                            context = context,
                            scope = scope,
                            value = it,
                        )
                    },
                )
            }
        }
    }
}

