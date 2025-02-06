package com.skyd.rays.model.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.skyd.rays.model.bean.URI_STRING_SHARE_PACKAGE_TABLE_NAME
import com.skyd.rays.model.bean.UriStringSharePackageBean

@Dao
interface UriStringSharePackageDao {
    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updatePackage(uriStringSharePackageBean: UriStringSharePackageBean)

    @Transaction
    @Query("SELECT * FROM $URI_STRING_SHARE_PACKAGE_TABLE_NAME")
    suspend fun getAllPackage(): List<UriStringSharePackageBean>

    @Transaction
    @Query("DELETE FROM $URI_STRING_SHARE_PACKAGE_TABLE_NAME WHERE ${UriStringSharePackageBean.PACKAGE_NAME_COLUMN} LIKE :pkgName")
    suspend fun deletePackage(pkgName: String): Int
}