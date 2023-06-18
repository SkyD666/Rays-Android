package com.skyd.rays.ui.screen.settings.searchconfig

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Domain
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.JoinInner
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skyd.rays.R
import com.skyd.rays.base.LoadUiIntent
import com.skyd.rays.config.allSearchDomain
import com.skyd.rays.model.bean.SearchDomainBean
import com.skyd.rays.model.preference.search.IntersectSearchBySpacePreference
import com.skyd.rays.model.preference.search.UseRegexSearchPreference
import com.skyd.rays.ui.component.BaseSettingsItem
import com.skyd.rays.ui.component.CategorySettingsItem
import com.skyd.rays.ui.component.RaysTopBar
import com.skyd.rays.ui.component.RaysTopBarStyle
import com.skyd.rays.ui.component.SwitchSettingsItem
import com.skyd.rays.ui.component.dialog.WaitingDialog
import com.skyd.rays.ui.local.LocalIntersectSearchBySpace
import com.skyd.rays.ui.local.LocalUseRegexSearch

const val SEARCH_CONFIG_SCREEN_ROUTE = "searchConfigScreen"

@Composable
fun SearchConfigScreen(viewModel: SearchConfigViewModel = hiltViewModel()) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val useRegexSearch = LocalUseRegexSearch.current
    val intersectSearchBySpace = LocalIntersectSearchBySpace.current
    var openWaitingDialog by rememberSaveable { mutableStateOf(false) }
    val uiState by viewModel.uiStateFlow.collectAsStateWithLifecycle()
    val loadUiIntent by viewModel.loadUiIntentFlow.collectAsStateWithLifecycle(initialValue = null)

    LaunchedEffect(Unit) {
        viewModel.sendUiIntent(SearchConfigIntent.GetSearchDomain)
    }

    Scaffold(
        topBar = {
            RaysTopBar(
                style = RaysTopBarStyle.Large,
                scrollBehavior = scrollBehavior,
                title = { Text(text = stringResource(R.string.search_config_screen_name)) },
            )
        }
    ) { paddingValues ->
        val selected = remember { mutableStateMapOf<Int, SnapshotStateMap<Int, Boolean>>() }
        val tables = remember { allSearchDomain.keys.toList() }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = paddingValues,
        ) {
            item {
                CategorySettingsItem(
                    text = stringResource(id = R.string.search_config_screen_common_category)
                )
            }
            item {
                SwitchSettingsItem(
                    icon = Icons.Default.Code,
                    text = stringResource(id = R.string.search_config_screen_use_regex),
                    description = stringResource(id = R.string.search_config_screen_use_regex_description),
                    checked = useRegexSearch,
                    onCheckedChange = {
                        UseRegexSearchPreference.put(
                            context = context,
                            scope = scope,
                            value = it
                        )
                    },
                )
            }
            item {
                SwitchSettingsItem(
                    icon = Icons.Default.JoinInner,
                    text = stringResource(id = R.string.search_config_screen_intersect_search_by_space),
                    description = stringResource(id = R.string.search_config_screen_intersect_search_by_space_description),
                    checked = intersectSearchBySpace,
                    onCheckedChange = {
                        IntersectSearchBySpacePreference.put(
                            context = context, scope = scope, value = it
                        )
                    },
                )
            }
            item {
                CategorySettingsItem(
                    text = stringResource(id = R.string.search_config_screen_domain_category)
                )
            }
            val searchDomainResultUiState = uiState.searchDomainResultUiState
            if (searchDomainResultUiState is SearchDomainResultUiState.Success) {
                repeat(tables.size) { tableIndex ->
                    selected[tableIndex] = mutableStateMapOf()
                    item {
                        SearchDomainItem(
                            selected = selected[tableIndex]!!,
                            table = tables[tableIndex],
                            searchDomain = searchDomainResultUiState.searchDomainMap,
                            onSetSearchDomain = {
                                viewModel.sendUiIntent(SearchConfigIntent.SetSearchDomain(it))
                            }
                        )
                    }
                }
            }
        }
        loadUiIntent?.also {
            when (it) {
                is LoadUiIntent.Error -> Unit
                is LoadUiIntent.Loading -> {
                    openWaitingDialog = it.isShow
                }
            }
        }
        WaitingDialog(visible = openWaitingDialog)
    }
}

@Composable
fun SearchDomainItem(
    selected: SnapshotStateMap<Int, Boolean>,
    table: Pair<String, Int>,
    searchDomain: Map<String, Boolean>,
    onSetSearchDomain: (SearchDomainBean) -> Unit,
    icon: Painter = rememberVectorPainter(image = Icons.Default.Domain),
) {
    val (tableName, tableDisplayName) = table
    BaseSettingsItem(
        icon = icon,
        text = stringResource(id = tableDisplayName),
        onClick = {},
        description = {
            val columns = allSearchDomain[table].orEmpty()
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                repeat(columns.size) { columnIndex ->
                    val (columnName, columnDisplayName) = columns[columnIndex]
                    selected[columnIndex] = searchDomain["${tableName}/${columnName}"] ?: false

                    FilterChip(
                        selected = selected[columnIndex] ?: false,
                        onClick = {
                            selected[columnIndex] = !(selected[columnIndex] ?: false)
                            onSetSearchDomain(
                                SearchDomainBean(
                                    tableName = tableName,
                                    columnName = columnName,
                                    search = selected[columnIndex] ?: false
                                )
                            )
                        },
                        label = { Text(text = stringResource(id = columnDisplayName)) },
                        leadingIcon = if (selected[columnIndex] == true) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Done,
                                    contentDescription = null,
                                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                                )
                            }
                        } else {
                            null
                        }
                    )
                }
            }
        }
    )
}
