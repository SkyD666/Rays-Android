package com.skyd.rays.model.respository

import android.database.DatabaseUtils
import androidx.core.net.toUri
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.sqlite.db.SimpleSQLiteQuery
import com.skyd.rays.appContext
import com.skyd.rays.base.BaseRepository
import com.skyd.rays.config.allSearchDomain
import com.skyd.rays.ext.catchMap
import com.skyd.rays.ext.dataStore
import com.skyd.rays.ext.flowOf
import com.skyd.rays.ext.getOrDefault
import com.skyd.rays.ext.safeDbVariableNumber
import com.skyd.rays.model.bean.STICKER_TABLE_NAME
import com.skyd.rays.model.bean.StickerBean
import com.skyd.rays.model.bean.StickerWithTags
import com.skyd.rays.model.bean.TagBean
import com.skyd.rays.model.db.dao.SearchDomainDao
import com.skyd.rays.model.db.dao.cache.StickerShareTimeDao
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
import kotlinx.coroutines.flow.combine
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
import kotlin.math.pow

class SearchRepository @Inject constructor(
    private val stickerDao: StickerDao,
    private val stickerShareTimeDao: StickerShareTimeDao,
    private val pagingConfig: PagingConfig,
) : BaseRepository() {
    fun requestStickerWithTagsList(keyword: String): List<StickerWithTags> = runBlocking {
        stickerDao.getStickerWithTagsList(genSql(k = keyword)).first()
    }

    fun requestStickerUuidList(keyword: String): Flow<List<String>> = flow {
        emit(stickerDao.getStickerUuidList(genSql(k = keyword, field = StickerBean.UUID_COLUMN)))
    }.flowOn(Dispatchers.IO)

    fun requestStickerWithTagsListFlow(
        keyword: String,
    ): Flow<List<StickerWithTags>> = flow { emit(genSql(keyword)) }
        .flatMapConcat {
            stickerDao.getStickerWithTagsList(it)
                .flowOn(Dispatchers.IO)
                .distinctUntilChanged()
        }
        .catchMap { emptyList() }
        .flowOn(Dispatchers.IO)

    fun requestStickerWithTagsListWithAllSearchDomain(
        keyword: String,
    ): Flow<Pager<Int, StickerWithTags>> = flow {
        val sql = genSql(k = keyword, useSearchDomain = { _, _ -> true })
        emit(Pager(pagingConfig) { stickerDao.getStickerWithTagsPaging(sql) })
    }.flowOn(Dispatchers.IO)

    data class SearchResult(
        val stickerWithTagsList: List<StickerWithTags>?,
        val msg: String? = null,
        val isRegexInvalid: SearchRegexInvalidException? = null,
    )

    fun requestStickerWithTagsList(): Flow<SearchResult> = appContext.dataStore.flowOf(
        QueryPreference, SearchResultSortPreference, SearchResultReversePreference
    ).debounce(70).flatMapLatest { (query, _, _) ->
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
    }.flowOn(Dispatchers.IO)

    fun requestDeleteStickerWithTagsDetail(
        stickerUuids: Collection<String>,
    ): Flow<Collection<String>> = flow {
        stickerUuids.distinct().safeDbVariableNumber {
            stickerDao.deleteStickerWithTags(it)
            stickerShareTimeDao.deleteShareTimeByUuids(it)
        }
        emit(stickerUuids)
    }.flowOn(Dispatchers.IO)

    fun requestStickersNotIn(
        keyword: String,
        selectedStickerUuids: Collection<String>,
    ): Flow<List<String>> = flow {
        emit(requestStickerUuidList(keyword).first().filter { it !in selectedStickerUuids })
    }.flowOn(Dispatchers.IO)

    fun requestSearchBarPopularTags(count: Int): Flow<List<String>> = combine(
        stickerDao.getRecentSharedStickers(count = count shr 1),
        stickerDao.getPopularStickersList(count = count shr 1),
    ) { recentSharedStickers, popularStickers ->
        recentSharedStickers.toMutableSet().apply { addAll(popularStickers) }.map {
            it to stickerShareTimeDao.getShareTimeByUuid(it.sticker.uuid)
        }
    }.distinctUntilChanged().map { stickersList ->
        // Step 2: Sort the list
        val sortedDataList = stickersList.sortedWith(
            compareByDescending<Pair<StickerWithTags, List<Long>>> {
                it.second.sum()
            }.thenByDescending { it.first.sticker.shareCount },
        )

        // Step 3: Count tag frequencies and add weights
        val tagFrequency = mutableMapOf<String, Double>()
        for ((index, data) in sortedDataList.withIndex()) {
            val weight = (sortedDataList.size - index).toDouble().pow(4)  // weight factor
            for (tag in data.first.tags) {
                // As the number of times a tag appears increases,
                // reduce its new weight to avoid the first few tags being difficult to change
                val newWeight = weight *
                        (1f / tagFrequency.getOrDefault(tag.tag, 1.0)
                            .coerceAtLeast(1.0)).pow(4)
                tagFrequency[tag.tag] = tagFrequency.getOrDefault(tag.tag, 0.0) + newWeight
            }
        }

        // Step 4: Sort tags by their frequencies
        tagFrequency.entries.sortedByDescending { it.value }.map { it.key }
    }.flowOn(Dispatchers.IO)

    fun requestExportStickers(stickerUuids: Collection<String>): Flow<Int> = flow {
        val exportStickerDir = appContext.dataStore.getOrDefault(ExportStickerDirPreference)
        check(exportStickerDir.isNotBlank()) { "exportStickerDir is null" }
        var successCount = 0
        stickerUuids.forEach {
            runCatching {
                exportSticker(uuid = it, outputDir = exportStickerDir.toUri())
            }.onSuccess {
                successCount++
            }.onFailure {
                it.printStackTrace()
            }
        }
        emit(successCount)
    }.flowOn(Dispatchers.IO)

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

        suspend fun genSql(
            k: String,
            field: String = "*",
            useSearchDomain: suspend (tableName: String, columnName: String) -> Boolean =
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
                            "SELECT $field FROM $STICKER_TABLE_NAME WHERE ${
                                getFilter(k = s, useSearchDomain = useSearchDomain)
                            } \n"
                        )
                    }
                }
                SimpleSQLiteQuery(sql)
            } else {
                SimpleSQLiteQuery(
                    "SELECT $field FROM $STICKER_TABLE_NAME WHERE ${
                        getFilter(k = k, useSearchDomain = useSearchDomain)
                    }"
                )
            }
        }

        private suspend fun getFilter(
            k: String,
            useSearchDomain: suspend (tableName: String, columnName: String) -> Boolean,
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