package com.skyd.rays.model.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.skyd.rays.model.bean.API_GRANT_PACKAGE_TABLE_NAME
import com.skyd.rays.model.bean.ApiGrantPackageBean

@Dao
interface ApiGrantPackageDao {
    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updatePackage(apiGrantPackageBean: ApiGrantPackageBean)

    @Transaction
    @Query("SELECT * FROM $API_GRANT_PACKAGE_TABLE_NAME")
    suspend fun getAllPackage(): List<ApiGrantPackageBean>

    @Transaction
    @Query("DELETE FROM $API_GRANT_PACKAGE_TABLE_NAME WHERE ${ApiGrantPackageBean.PACKAGE_NAME_COLUMN} LIKE :pkgName")
    suspend fun deletePackage(pkgName: String): Int

    @Transaction
    @Query("SELECT ${ApiGrantPackageBean.ENABLED_COLUMN} FROM $API_GRANT_PACKAGE_TABLE_NAME WHERE ${ApiGrantPackageBean.PACKAGE_NAME_COLUMN} LIKE :pkgName")
    suspend fun packageEnable(pkgName: String): Boolean
}