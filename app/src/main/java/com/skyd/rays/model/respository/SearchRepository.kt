package com.skyd.rays.model.respository

import android.database.DatabaseUtils
import android.net.Uri
import androidx.sqlite.db.SimpleSQLiteQuery
import com.skyd.rays.appContext
import com.skyd.rays.base.BaseRepository
import com.skyd.rays.config.allSearchDomain
import com.skyd.rays.ext.catchMap
import com.skyd.rays.ext.dataStore
import com.skyd.rays.ext.getOrDefault
import com.skyd.rays.model.bean.STICKER_TABLE_NAME
import com.skyd.rays.model.bean.StickerBean
import com.skyd.rays.model.bean.StickerWithTags
import com.skyd.rays.model.bean.TagBean
import com.skyd.rays.model.db.dao.SearchDomainDao
import com.skyd.rays.model.db.dao.sticker.StickerDao
import com.skyd.rays.model.preference.ExportStickerDirPreference
import com.skyd.rays.model.preference.search.IntersectSearchBySpacePreference
import com.skyd.rays.model.preference.search.QueryPreference
import com.skyd.rays.model.preference.search.SearchResultReversePreference
import com.skyd.rays.model.preference.search.SearchResultSortPreference
import com.skyd.rays.model.preference.search.UseRegexSearchPreference
import com.skyd.rays.util.exportSticker
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class SearchRepository @Inject constructor(
    private val stickerDao: StickerDao,
) : BaseRepository() {
    fun requestStickerWithTagsList(keyword: String): List<StickerWithTags> = runBlocking {
        stickerDao.getStickerWithTagsList(genSql(k = keyword)).first()
    }

    fun requestStickerWithTagsListFlow(keyword: String): Flow<List<StickerWithTags>> {
        return flow { emit(genSql(keyword)) }
            .flowOn(Dispatchers.IO)
            .flatMapConcat {
                stickerDao.getStickerWithTagsList(it)
                    .flowOn(Dispatchers.IO)
                    .distinctUntilChanged()
            }
            .catchMap { emptyList() }
    }

    fun requestStickerWithTagsListWithAllSearchDomain(keyword: String): Flow<List<StickerWithTags>> {
        return flow { emit(genSql(k = keyword, useSearchDomain = { _, _ -> true })) }
            .flowOn(Dispatchers.IO)
            .flatMapConcat {
                stickerDao.getStickerWithTagsList(it)
                    .flowOn(Dispatchers.IO)
                    .distinctUntilChanged()
            }
            .catchMap { emptyList() }
    }

    data class SearchResult(
        val stickerWithTagsList: List<StickerWithTags>?,
        val msg: String? = null,
        val isRegexInvalid: SearchRegexInvalidException? = null,
    )

    fun requestStickerWithTagsList(): Flow<SearchResult> {
        return appContext.dataStore.data
            .debounce(70)
            .map {
                Triple(
                    it[QueryPreference.key] ?: QueryPreference.default,
                    it[SearchResultSortPreference.key] ?: SearchResultSortPreference.default,
                    it[SearchResultReversePreference.key] ?: SearchResultReversePreference.default,
                )
            }
            .distinctUntilChanged()
            .flatMapLatest { (query, _, _) ->
                var msg: String? = null
                var isRegexInvalid: SearchRegexInvalidException? = null
                val sql = runCatching { genSql(query) }.getOrElse {
                    if (it is SearchRegexInvalidException) isRegexInvalid = it
                    msg = it.message.toString()
                    null
                }
                if (sql == null) {
                    flowOf(
                        SearchResult(
                            stickerWithTagsList = null,
                            msg = msg,
                            isRegexInvalid = isRegexInvalid
                        )
                    )
                } else {
                    stickerDao.getStickerWithTagsList(sql).map { list ->
                        SearchResult(stickerWithTagsList = sortSearchResultList(list))
                    }.flowOn(Dispatchers.IO)
                }
            }
            .flowOn(Dispatchers.IO)
    }

    suspend fun requestDeleteStickerWithTagsDetail(stickerUuids: List<String>): Flow<List<String>> {
        return flowOnIo {
            stickerDao.deleteStickerWithTags(stickerUuids)
            emit(stickerUuids)
        }
    }

    fun requestSearchBarPopularTags(count: Int): Flow<List<Pair<String, Float>>> {
        return stickerDao.getPopularStickersList(count = count)
            .distinctUntilChanged()
            .map { popularStickersList ->
                val tagsMap: MutableMap<Pair<String, String>, Long> = mutableMapOf()
                val tagsCountMap: MutableMap<Pair<String, String>, Long> = mutableMapOf()
                val stickerUuidCountMap: MutableMap<String, Long> = mutableMapOf()
                popularStickersList.forEach {
                    it.tags.forEach { tag ->
                        val tagString = tag.tag
                        if (tagString.length < 6) {
                            tagsCountMap[tagString to it.sticker.uuid] = tagsCountMap
                                .getOrDefault(tagString to it.sticker.uuid, 0) + 1
                            tagsMap[tagString to it.sticker.uuid] = tagsMap
                                .getOrDefault(
                                    tagString to it.sticker.uuid,
                                    0
                                ) + it.sticker.shareCount
                        }
                    }
                    stickerUuidCountMap[it.sticker.uuid] = 0
                }
                tagsCountMap.forEach { (t, u) ->
                    tagsMap[t] = tagsMap.getOrDefault(t, 0) * u
                }
                var result = tagsMap.toList().sortedByDescending { (_, value) -> value }
                result = result.filter {
                    val stickUuid = it.first.second
                    val cnt = stickerUuidCountMap[stickUuid]
                    if (cnt != null) {
                        // 限制每个表情包只能推荐两个标签
                        if (cnt >= 2) {
                            false
                        } else {
                            stickerUuidCountMap[stickUuid] = cnt + 1
                            true
                        }
                    } else {
                        false
                    }
                }.distinctBy { it.first.first }
                val maxPopularValue = result.getOrNull(0)?.second ?: 1
                result.map { it.first.first to it.second.toFloat() / maxPopularValue }
            }.flowOn(Dispatchers.IO)
    }

    suspend fun requestExportStickers(stickerUuids: List<String>): Flow<Int> {
        return flowOnIo {
            val exportStickerDir = appContext.dataStore.getOrDefault(ExportStickerDirPreference)
            check(exportStickerDir.isNotBlank()) { "exportStickerDir is null" }
            var successCount = 0
            stickerUuids.forEach {
                runCatching {
                    exportSticker(uuid = it, outputDir = Uri.parse(exportStickerDir))
                }.onSuccess {
                    successCount++
                }.onFailure {
                    it.printStackTrace()
                }
            }
            emit(successCount)
        }
    }

    private fun sortSearchResultList(
        unsortedUnreversedData: List<StickerWithTags>,
        applyReverse: Boolean = true
    ): List<StickerWithTags> =
        when (appContext.dataStore.getOrDefault(SearchResultSortPreference)) {
            "CreateTime" -> unsortedUnreversedData.sortStickers(applyReverse) {
                it.sticker.createTime
            }

            "ModifyTime" -> unsortedUnreversedData.sortStickers(
                applyReverse,
                compareBy({ it.sticker.modifyTime }, { it.sticker.createTime })
            )

            "TagCount" -> unsortedUnreversedData.sortStickers(
                applyReverse,
                compareBy({ it.tags.size }, { it.sticker.createTime })
            )

            "Title" -> unsortedUnreversedData.sortStickers(
                applyReverse,
                compareBy({ it.sticker.title }, { it.sticker.createTime })
            )

            "ClickCount" -> unsortedUnreversedData.sortStickers(
                applyReverse,
                compareBy({ it.sticker.clickCount }, { it.sticker.createTime })
            )

            "ShareCount" -> unsortedUnreversedData.sortStickers(
                applyReverse,
                compareBy({ it.sticker.shareCount }, { it.sticker.createTime })
            )

            else -> unsortedUnreversedData.sortStickers(applyReverse) {
                it.sticker.createTime
            }
        }

    private fun <R : Comparable<R>> List<StickerWithTags>.sortStickers(
        applyReverse: Boolean = true,
        selector: (StickerWithTags) -> R?
    ): List<StickerWithTags> {
        return if (applyReverse && appContext.dataStore.getOrDefault(SearchResultReversePreference)) {
            sortedByDescending(selector)
        } else {
            sortedBy(selector)
        }
    }

    private fun List<StickerWithTags>.sortStickers(
        applyReverse: Boolean = true,
        comparator: Comparator<StickerWithTags>
    ): List<StickerWithTags> {
        return if (applyReverse && appContext.dataStore.getOrDefault(SearchResultReversePreference)) {
            sortedWith(comparator).reversed()
        } else {
            sortedWith(comparator)
        }
    }

    class SearchRegexInvalidException(message: String?) : IllegalArgumentException(message)

    companion object {
        @EntryPoint
        @InstallIn(SingletonComponent::class)
        interface HomeRepositoryEntryPoint {
            val searchDomainDao: SearchDomainDao
        }

        fun genSql(
            k: String,
            useSearchDomain: (tableName: String, columnName: String) -> Boolean =
                { tableName, columnName ->
                    EntryPointAccessors.fromApplication(
                        appContext, HomeRepositoryEntryPoint::class.java
                    ).searchDomainDao.getSearchDomain(tableName, columnName)
                },
            intersectSearchBySpace: Boolean = appContext.dataStore.getOrDefault(
                IntersectSearchBySpacePreference
            ),
        ): SimpleSQLiteQuery {
            val useRegexSearch = appContext.dataStore.getOrDefault(UseRegexSearchPreference)
            if (useRegexSearch) {
                // Check Regex format
                runCatching { k.toRegex() }.onFailure {
                    throw SearchRegexInvalidException(it.message)
                }
            }

            return if (intersectSearchBySpace) {
                // 以多个连续的空格/制表符/换行符分割
                val keywords = k.trim().split("\\s+".toRegex()).toSet()
                val sql = buildString {
                    keywords.forEachIndexed { index, s ->
                        if (index > 0) append("INTERSECT \n")
                        append(
                            "SELECT * FROM $STICKER_TABLE_NAME WHERE ${
                                getFilter(k = s, useSearchDomain = useSearchDomain)
                            } \n"
                        )
                    }
                }
                SimpleSQLiteQuery(sql)
            } else {
                SimpleSQLiteQuery(
                    "SELECT * FROM $STICKER_TABLE_NAME WHERE ${
                        getFilter(k = k, useSearchDomain = useSearchDomain)
                    }"
                )
            }
        }

        private fun getFilter(
            k: String,
            useSearchDomain: (tableName: String, columnName: String) -> Boolean,
        ): String {
            if (k.isBlank()) return "1"

            val useRegexSearch = appContext.dataStore.getOrDefault(UseRegexSearchPreference)

            var filter = "0"

            // 转义输入，防止SQL注入
            val keyword = if (useRegexSearch) {
                // Check Regex format
                runCatching { k.toRegex() }.onFailure {
                    throw SearchRegexInvalidException(it.message)
                }
                DatabaseUtils.sqlEscapeString(k)
            } else {
                DatabaseUtils.sqlEscapeString("%$k%")
            }

            val tables = allSearchDomain.keys
            for (table in tables) {
                val columns = allSearchDomain[table].orEmpty()

                if (table.first == STICKER_TABLE_NAME) {
                    for (column in columns) {
                        if (!useSearchDomain(table.first, column.first)) {
                            continue
                        }
                        filter += if (useRegexSearch) {
                            " OR ${column.first} REGEXP $keyword"
                        } else {
                            " OR ${column.first} LIKE $keyword"
                        }
                    }
                } else {
                    var hasQuery = false
                    var subSelect =
                        "(SELECT DISTINCT ${TagBean.STICKER_UUID_COLUMN} FROM ${table.first} WHERE 0 "
                    for (column in columns) {
                        if (!useSearchDomain(table.first, column.first)) {
                            continue
                        }
                        subSelect += if (useRegexSearch) {
                            " OR ${column.first} REGEXP $keyword"
                        } else {
                            " OR ${column.first} LIKE $keyword"
                        }
                        hasQuery = true
                    }
                    if (!hasQuery) {
                        continue
                    }
                    subSelect += ")"
                    filter += " OR ${StickerBean.UUID_COLUMN} IN $subSelect"
                }
            }

            if (filter == "0") {
                filter += " OR 1"
            }
            return filter
        }
    }
}