package com.skyd.rays.model.db.dao

import androidx.room.*
import com.skyd.rays.model.bean.*
import com.skyd.rays.model.bean.SearchDomainBean.Companion.COLUMN_NAME_COLUMN
import com.skyd.rays.model.bean.SearchDomainBean.Companion.SEARCH_COLUMN
import com.skyd.rays.model.bean.SearchDomainBean.Companion.TABLE_NAME_COLUMN

@Dao
interface SearchDomainDao {
    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun setSearchDomain(searchDomainBean: SearchDomainBean)

    @Transaction
    @Query(
        """SELECT $SEARCH_COLUMN FROM $SEARCH_DOMAIN_TABLE_NAME
           WHERE $TABLE_NAME_COLUMN LIKE :tableName AND $COLUMN_NAME_COLUMN LIKE :columnName"""
    )
    fun getSearchDomainOrNull(tableName: String, columnName: String): Boolean?

    // 被选择的搜索域的个数
    @Transaction
    @Query("SELECT COUNT(*) FROM $SEARCH_DOMAIN_TABLE_NAME WHERE $SEARCH_COLUMN = 1")
    fun selectedSearchDomainCount(): Int

    @Transaction
    fun getSearchDomain(tableName: String, columnName: String): Boolean {
        val result = getSearchDomainOrNull(tableName, columnName)
        return result == true || selectedSearchDomainCount() == 0
    }

    @Transaction
    @Query(value = "SELECT * FROM $SEARCH_DOMAIN_TABLE_NAME")
    fun getAllSearchDomain(): List<SearchDomainBean>
}