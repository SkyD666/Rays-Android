package com.skyd.rays.ui.screen.search

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.skyd.rays.model.preference.search.SearchResultSortPreference
import com.skyd.rays.ui.local.LocalSearchResultSort


@Composable
internal fun SearchResultSortMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val searchResultSort = LocalSearchResultSort.current

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest
    ) {
        SearchResultSortPreference.sortList.forEach {
            DropdownMenuItem(
                text = { Text(text = SearchResultSortPreference.toDisplayName(it)) },
                leadingIcon = {
                    if (searchResultSort == it) {
                        Icon(
                            imageVector = Icons.Default.Done,
                            contentDescription = null
                        )
                    }
                },
                onClick = {
                    SearchResultSortPreference.put(
                        context = context,
                        scope = scope,
                        value = it
                    )
                    onDismissRequest()
                },
            )
        }
    }
}
