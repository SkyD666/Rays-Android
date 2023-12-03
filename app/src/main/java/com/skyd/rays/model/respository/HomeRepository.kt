package com.skyd.rays.model.respository

import android.database.DatabaseUtils
import androidx.sqlite.db.SimpleSQLiteQuery
import com.skyd.rays.appContext
import com.skyd.rays.base.BaseRepository
import com.skyd.rays.config.allSearchDomain
import com.skyd.rays.ext.dataStore
import com.skyd.rays.ext.getOrDefault
import com.skyd.rays.model.bean.STICKER_TABLE_NAME
import com.skyd.rays.model.bean.StickerBean
import com.skyd.rays.model.bean.StickerWithTags
import com.skyd.rays.model.bean.TagBean
import com.skyd.rays.model.db.dao.SearchDomainDao
import com.skyd.rays.model.db.dao.TagDao
import com.skyd.rays.model.db.dao.sticker.StickerDao
import com.skyd.rays.model.preference.search.IntersectSearchBySpacePreference
import com.skyd.rays.model.preference.search.UseRegexSearchPreference
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class HomeRepository @Inject constructor(
    private val stickerDao: StickerDao,
    private val tagDao: TagDao
) : BaseRepository() {
    fun requestRecommendTags(): Flow<List<TagBean>> {
        return tagDao.getRecommendTagsList(count = 10).distinctUntilChanged().flowOn(Dispatchers.IO)
    }

    fun requestRandomTags(): Flow<List<TagBean>> {
        return tagDao.getRandomTagsList(count = 10).distinctUntilChanged().flowOn(Dispatchers.IO)
    }

    fun requestRecentCreateStickers(): Flow<List<StickerWithTags>> {
        return stickerDao.getRecentCreateStickersList(count = 10)
            .distinctUntilChanged()
            .flowOn(Dispatchers.IO)
    }

    fun requestMostSharedStickers(): Flow<List<StickerWithTags>> {
        return stickerDao.getMostSharedStickersList(count = 10)
            .distinctUntilChanged()
            .flowOn(Dispatchers.IO)
    }

    companion object {
        @EntryPoint
        @InstallIn(SingletonComponent::class)
        interface HomeRepositoryEntryPoint {
            val searchDomainDao: SearchDomainDao
        }

        fun genSql(k: String): SimpleSQLiteQuery {
            val hiltEntryPoint = EntryPointAccessors.fromApplication(
                appContext, HomeRepositoryEntryPoint::class.java
            )
            // 是否使用多个关键字并集查询
            val intersectSearchBySpace =
                appContext.dataStore.getOrDefault(IntersectSearchBySpacePreference)
            return if (intersectSearchBySpace) {
                // 以多个连续的空格/制表符/换行符分割
                val keywords = k.trim().split("\\s+".toRegex()).toSet()
                val sql = buildString {
                    keywords.forEachIndexed { index, s ->
                        if (index > 0) append("INTERSECT \n")
                        append(
                            "SELECT * FROM $STICKER_TABLE_NAME WHERE ${
                                getFilter(s, hiltEntryPoint.searchDomainDao)
                            } \n"
                        )
                    }
                }
                SimpleSQLiteQuery(sql)
            } else {
                SimpleSQLiteQuery(
                    "SELECT * FROM $STICKER_TABLE_NAME WHERE ${
                        getFilter(k, hiltEntryPoint.searchDomainDao)
                    }"
                )
            }
        }

        private fun getFilter(k: String, searchDomainDao: SearchDomainDao): String {
            if (k.isBlank()) return "1"

            val useRegexSearch =
                appContext.dataStore.getOrDefault(UseRegexSearchPreference)

            var filter = "0"

            // 转义输入，防止SQL注入
            val keyword = if (useRegexSearch) {
                // 检查正则表达式是否有效
                runCatching { k.toRegex() }.onFailure { error(it.message.orEmpty()) }
                DatabaseUtils.sqlEscapeString(k)
            } else {
                DatabaseUtils.sqlEscapeString("%$k%")
            }

            val tables = allSearchDomain.keys
            for (table in tables) {
                val columns = allSearchDomain[table].orEmpty()

                if (table.first == STICKER_TABLE_NAME) {
                    for (column in columns) {
                        if (!searchDomainDao.getSearchDomain(table.first, column.first)) {
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
                        if (!searchDomainDao.getSearchDomain(table.first, column.first)) {
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