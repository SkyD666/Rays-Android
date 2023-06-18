package com.skyd.rays.model.db.dao

import androidx.room.*
import com.skyd.rays.model.bean.*

@Dao
interface ApiGrantPackageDao {
    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun updatePackage(apiGrantPackageBean: ApiGrantPackageBean)

    @Transaction
    @Query("SELECT * FROM $API_GRANT_PACKAGE_TABLE_NAME")
    fun getAllPackage(): List<ApiGrantPackageBean>

    @Transaction
    @Query("DELETE FROM $API_GRANT_PACKAGE_TABLE_NAME WHERE ${ApiGrantPackageBean.PACKAGE_NAME_COLUMN} LIKE :pkgName")
    fun deletePackage(pkgName: String): Int

    @Transaction
    @Query("SELECT ${ApiGrantPackageBean.ENABLED_COLUMN} FROM $API_GRANT_PACKAGE_TABLE_NAME WHERE ${ApiGrantPackageBean.PACKAGE_NAME_COLUMN} LIKE :pkgName")
    fun packageEnable(pkgName: String): Boolean
}