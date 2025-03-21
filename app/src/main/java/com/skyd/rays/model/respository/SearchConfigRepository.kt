package com.skyd.rays.model.respository

import com.skyd.rays.base.BaseRepository
import com.skyd.rays.model.bean.SearchDomainBean
import com.skyd.rays.model.db.dao.SearchDomainDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class SearchConfigRepository @Inject constructor(
    private val searchDomainDao: SearchDomainDao
) : BaseRepository() {
    fun requestGetSearchDomain(): Flow<Map<String, Boolean>> = flow {
        val map = mutableMapOf<String, Boolean>()
        searchDomainDao.getAllSearchDomain().forEach {
            map["${it.tableName}/${it.columnName}"] = it.search
        }
        emit(map)
    }.flowOn(Dispatchers.IO)

    fun requestSetSearchDomain(
        searchDomainBean: SearchDomainBean
    ): Flow<SearchDomainBean> = flow {
        searchDomainDao.setSearchDomain(searchDomainBean)
        emit(searchDomainBean)
    }.flowOn(Dispatchers.IO)
}