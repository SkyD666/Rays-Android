package com.skyd.rays.db.dao

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

    @Transaction
    fun getSearchDomain(tableName: String, columnName: String): Boolean {
        return getSearchDomainOrNull(tableName, columnName)
            ?: if (tableName == STICKER_TABLE_NAME &&
                (columnName == StickerBean.TITLE_COLUMN ||
                        columnName == StickerBean.UUID_COLUMN)
            ) true else tableName == TAG_TABLE_NAME && columnName == TagBean.TAG_COLUMN
    }

    @Transaction
    @Query(value = "SELECT * FROM $SEARCH_DOMAIN_TABLE_NAME")
    fun getAllSearchDomain(): List<SearchDomainBean>
}