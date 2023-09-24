package com.skyd.rays.model.respository

import android.database.DatabaseUtils
import android.net.Uri
import androidx.sqlite.db.SimpleSQLiteQuery
import com.skyd.rays.appContext
import com.skyd.rays.base.BaseData
import com.skyd.rays.base.BaseRepository
import com.skyd.rays.config.allSearchDomain
import com.skyd.rays.ext.dataStore
import com.skyd.rays.ext.get
import com.skyd.rays.model.bean.STICKER_TABLE_NAME
import com.skyd.rays.model.bean.StickerBean
import com.skyd.rays.model.bean.StickerWithTags
import com.skyd.rays.model.bean.TagBean
import com.skyd.rays.model.db.dao.SearchDomainDao
import com.skyd.rays.model.db.dao.StickerDao
import com.skyd.rays.model.preference.ExportStickerDirPreference
import com.skyd.rays.model.preference.search.IntersectSearchBySpacePreference
import com.skyd.rays.model.preference.search.UseRegexSearchPreference
import com.skyd.rays.util.exportSticker
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class HomeRepository @Inject constructor(private val stickerDao: StickerDao) : BaseRepository() {
    suspend fun requestStickerWithTagsList(keyword: String): Flow<BaseData<List<StickerWithTags>>> {
        return flow {
            emitBaseData(BaseData<List<StickerWithTags>>().apply {
                code = 0
                data = stickerDao.getStickerWithTagsList(genSql(keyword))
            })
        }
    }

    suspend fun requestStickerWithTagsDetail(stickerUuid: String): Flow<BaseData<StickerWithTags>> {
        return flow {
            val stickerWithTags = stickerDao.getStickerWithTags(stickerUuid)
            emitBaseData(BaseData<StickerWithTags>().apply {
                code = if (stickerWithTags == null) 1 else 0
                data = stickerWithTags
            })
        }
    }

    suspend fun requestDeleteStickerWithTagsDetail(stickerUuids: List<String>): Flow<BaseData<Int>> {
        return flow {
            emitBaseData(BaseData<Int>().apply {
                code = 0
                data = stickerDao.deleteStickerWithTags(stickerUuids)
            })
        }
    }

    suspend fun requestAddClickCount(stickerUuid: String, count: Int = 1): Flow<BaseData<Int>> {
        return flow {
            emitBaseData(BaseData<Int>().apply {
                code = 0
                data = stickerDao.addClickCount(uuid = stickerUuid, count = count)
            })
        }
    }

    suspend fun requestPopularTags(count: Int): Flow<BaseData<List<Pair<String, Float>>>> {
        return flow {
            val popularStickersList = stickerDao.getPopularStickersList(count = count)
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
                            .getOrDefault(tagString to it.sticker.uuid, 0) + it.sticker.shareCount
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
            emitBaseData(BaseData<List<Pair<String, Float>>>().apply {
                code = 0
                data = result.map { it.first.first to it.second.toFloat() / maxPopularValue }
            })
        }
    }

    suspend fun requestExportStickers(stickerUuids: List<String>): Flow<BaseData<Int>> {
        return flow {
            val exportStickerDir = appContext.dataStore.get(ExportStickerDirPreference.key)
            check(exportStickerDir != null) { "exportStickerDir is null" }
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
            emitBaseData(BaseData<Int>().apply {
                code = 0
                data = successCount
            })
        }
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
                appContext.dataStore.get(IntersectSearchBySpacePreference.key) ?: true
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

            val useRegexSearch = appContext.dataStore.get(UseRegexSearchPreference.key) ?: false

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