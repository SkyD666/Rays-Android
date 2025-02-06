package com.skyd.rays.model.respository

import com.skyd.rays.base.BaseRepository
import com.skyd.rays.model.bean.SearchDomainBean
import com.skyd.rays.model.db.dao.SearchDomainDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchConfigRepository @Inject constructor(
    private val searchDomainDao: SearchDomainDao
) : BaseRepository() {
    fun requestGetSearchDomain(): Flow<Map<String, Boolean>> {
        return flowOnIo {
            val map = mutableMapOf<String, Boolean>()
            searchDomainDao.getAllSearchDomain().forEach {
                map["${it.tableName}/${it.columnName}"] = it.search
            }
            emit(map)
        }
    }

    fun requestSetSearchDomain(
        searchDomainBean: SearchDomainBean
    ): Flow<SearchDomainBean> {
        return flowOnIo {
            searchDomainDao.setSearchDomain(searchDomainBean)
            emit(searchDomainBean)
        }
    }
}