package com.skyd.rays.model.preference.search

import androidx.datastore.preferences.core.stringPreferencesKey
import com.skyd.rays.R
import com.skyd.rays.appContext
import com.skyd.rays.model.preference.BasePreference

object SearchResultSortPreference : BasePreference<String> {
    private const val SEARCH_RESULT_SORT = "searchResultSort"

    override val default = "CreateTime"
    override val key = stringPreferencesKey(SEARCH_RESULT_SORT)

    val sortList = arrayOf(
        "CreateTime",
        "ModifyTime",
        "TagCount",
        "Title",
        "ClickCount",
        "ShareCount",
    )

    fun toDisplayName(sort: String): String = when (sort) {
        "CreateTime" -> appContext.getString(R.string.sticker_create_time)
        "ModifyTime" -> appContext.getString(R.string.sticker_modify_time)
        "TagCount" -> appContext.getString(R.string.search_result_sort_tag_count)
        "Title" -> appContext.getString(R.string.search_result_sort_title)
        "ClickCount" -> appContext.getString(R.string.sticker_click_count)
        "ShareCount" -> appContext.getString(R.string.sticker_share_count)
        else -> appContext.getString(R.string.sticker_create_time)
    }
}