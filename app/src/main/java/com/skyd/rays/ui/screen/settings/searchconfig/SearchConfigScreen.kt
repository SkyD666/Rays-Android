package com.skyd.rays.ui.screen.settings.searchconfig

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Domain
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material.icons.outlined.JoinInner
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
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
import com.skyd.rays.base.mvi.MviEventListener
import com.skyd.rays.base.mvi.getDispatcher
import com.skyd.rays.config.allSearchDomain
import com.skyd.rays.model.bean.SearchDomainBean
import com.skyd.rays.model.preference.search.IntersectSearchBySpacePreference
import com.skyd.rays.model.preference.search.UseRegexSearchPreference
import com.skyd.rays.model.preference.search.imagesearch.AddScreenImageSearchPreference
import com.skyd.rays.model.preference.search.imagesearch.ImageSearchMaxResultCountPreference
import com.skyd.rays.ui.component.BaseSettingsItem
import com.skyd.rays.ui.component.CategorySettingsItem
import com.skyd.rays.ui.component.RaysTopBar
import com.skyd.rays.ui.component.RaysTopBarStyle
import com.skyd.rays.ui.component.SliderSettingsItem
import com.skyd.rays.ui.component.SwitchSettingsItem
import com.skyd.rays.ui.component.dialog.WaitingDialog
import com.skyd.rays.ui.local.LocalAddScreenImageSearch
import com.skyd.rays.ui.local.LocalImageSearchMaxResultCount
import com.skyd.rays.ui.local.LocalIntersectSearchBySpace
import com.skyd.rays.ui.local.LocalUseRegexSearch

const val SEARCH_CONFIG_SCREEN_ROUTE = "searchConfigScreen"

@Composable
fun SearchConfigScreen(viewModel: SearchConfigViewModel = hiltViewModel()) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val useRegexSearch = LocalUseRegexSearch.current
    val intersectSearchBySpace = LocalIntersectSearchBySpace.current
    val uiState by viewModel.viewState.collectAsStateWithLifecycle()

    val dispatch = viewModel.getDispatcher(startWith = SearchConfigIntent.GetSearchDomain)

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
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
                    imageVector = Icons.Outlined.Code,
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
                    imageVector = Icons.Outlined.JoinInner,
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
            val searchDomainResultUiState = uiState.searchDomainResultState
            if (searchDomainResultUiState is SearchDomainResultState.Success) {
                repeat(tables.size) { tableIndex ->
                    selected[tableIndex] = mutableStateMapOf()
                    item {
                        SearchDomainItem(
                            selected = selected[tableIndex]!!,
                            table = tables[tableIndex],
                            searchDomain = searchDomainResultUiState.searchDomainMap,
                            onSetSearchDomain = { dispatch(SearchConfigIntent.SetSearchDomain(it)) }
                        )
                    }
                }
            }
            item {
                CategorySettingsItem(text = stringResource(id = R.string.image_search_screen_name))
            }
            item {
                SwitchSettingsItem(
                    imageVector = Icons.Outlined.AddPhotoAlternate,
                    text = stringResource(id = R.string.search_config_screen_enable_add_screen_image_search),
                    description = stringResource(id = R.string.search_config_screen_enable_add_screen_image_search_description),
                    checked = LocalAddScreenImageSearch.current,
                    onCheckedChange = {
                        AddScreenImageSearchPreference.put(
                            context = context,
                            scope = scope,
                            value = it
                        )
                        if (it) dispatch(SearchConfigIntent.EnableAddScreenImageSearch)
                    },
                )
            }
            item {
                val maxCount = LocalImageSearchMaxResultCount.current
                var maxCountFloat by rememberSaveable { mutableFloatStateOf(maxCount.toFloat()) }
                SliderSettingsItem(
                    imageVector = Icons.Outlined.Code,
                    value = maxCountFloat,
                    valueFormater = { it.toInt().toString() },
                    text = stringResource(id = R.string.search_config_screen_image_search_max_result_count),
                    valueRange = 1f..100f,
                    onValueChange = {
                        ImageSearchMaxResultCountPreference.put(
                            context = context,
                            scope = scope,
                            value = it.toInt(),
                        )
                        maxCountFloat = it
                    },
                )
            }
        }

        MviEventListener(viewModel.singleEvent) { event ->
            when (event) {
                is SearchConfigEvent.EnableAddScreenImageSearchUiEvent.Failed -> snackbarHostState.showSnackbar(
                    context.getString(R.string.failed_info, event.msg),
                )
            }
        }

        WaitingDialog(visible = uiState.loadingDialog)
    }
}

@Composable
fun SearchDomainItem(
    selected: SnapshotStateMap<Int, Boolean>,
    table: Pair<String, Int>,
    searchDomain: Map<String, Boolean>,
    onSetSearchDomain: (SearchDomainBean) -> Unit,
    icon: Painter = rememberVectorPainter(image = Icons.Outlined.Domain),
) {
    val (tableName, tableDisplayName) = table
    BaseSettingsItem(
        painter = icon,
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
                    selected[columnIndex] = searchDomain["${tableName}/${columnName}"] == true

                    FilterChip(
                        selected = selected[columnIndex] == true,
                        onClick = {
                            selected[columnIndex] = selected[columnIndex] != true
                            onSetSearchDomain(
                                SearchDomainBean(
                                    tableName = tableName,
                                    columnName = columnName,
                                    search = selected[columnIndex] == true
                                )
                            )
                        },
                        label = { Text(text = stringResource(id = columnDisplayName)) },
                        leadingIcon = if (selected[columnIndex] == true) {
                            {
                                Icon(
                                    imageVector = Icons.Outlined.Done,
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
