package com.skyd.rays.model.preference.search

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.skyd.rays.R
import com.skyd.rays.appContext
import com.skyd.rays.ext.dataStore
import com.skyd.rays.ext.put
import com.skyd.rays.model.preference.BasePreference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object SearchResultSortPreference : BasePreference<String> {
    private const val SEARCH_RESULT_SORT = "searchResultSort"
    override val default = "CreateTime"

    val key = stringPreferencesKey(SEARCH_RESULT_SORT)

    val sortList = arrayOf(
        "CreateTime",
        "ModifyTime",
        "TagCount",
        "Title",
        "ClickCount",
        "ShareCount",
    )

    fun put(context: Context, scope: CoroutineScope, value: String) {
        scope.launch(Dispatchers.IO) {
            context.dataStore.put(key, value)
        }
    }

    override fun fromPreferences(preferences: Preferences): String = preferences[key] ?: default

    fun toDisplayName(sort: String): String = when (sort) {
        "CreateTime" -> appContext.getString(R.string.search_result_sort_create_time)
        "ModifyTime" -> appContext.getString(R.string.search_result_sort_modify_time)
        "TagCount" -> appContext.getString(R.string.search_result_sort_tag_count)
        "Title" -> appContext.getString(R.string.search_result_sort_title)
        "ClickCount" -> appContext.getString(R.string.search_result_sort_click_count)
        "ShareCount" -> appContext.getString(R.string.search_result_sort_share_count)
        else -> appContext.getString(R.string.search_result_sort_create_time)
    }
}