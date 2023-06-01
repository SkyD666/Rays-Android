package com.skyd.rays.model.db.dao

import androidx.room.*
import com.skyd.rays.model.bean.*

@Dao
interface UriStringSharePackageDao {
    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun updatePackage(uriStringSharePackageBean: UriStringSharePackageBean)

    @Transaction
    @Query("SELECT * FROM $URI_STRING_SHARE_PACKAGE_TABLE_NAME")
    fun getAllPackage(): List<UriStringSharePackageBean>

    @Transaction
    @Query("DELETE FROM $URI_STRING_SHARE_PACKAGE_TABLE_NAME WHERE ${UriStringSharePackageBean.PACKAGE_NAME_COLUMN} LIKE :pkgName")
    fun deletePackage(pkgName: String): Int
}