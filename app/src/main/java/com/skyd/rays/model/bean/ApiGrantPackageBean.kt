package com.skyd.rays.model.bean

import android.graphics.drawable.Drawable
import androidx.room.ColumnInfo
import androidx.room.Entity
import kotlinx.serialization.Serializable

const val API_GRANT_PACKAGE_TABLE_NAME = "ApiGrantPackage"

@Serializable
@Entity(
    tableName = API_GRANT_PACKAGE_TABLE_NAME,
    primaryKeys = [ApiGrantPackageBean.PACKAGE_NAME_COLUMN]
)
data class ApiGrantPackageBean(
    @ColumnInfo(name = PACKAGE_NAME_COLUMN)
    var packageName: String,
    @ColumnInfo(name = ENABLED_COLUMN)
    var enabled: Boolean,
) : BaseBean {
    companion object {
        const val PACKAGE_NAME_COLUMN = "packageName"
        const val ENABLED_COLUMN = "enabled"
    }
}

data class ApiGrantDataBean(
    var apiGrantPackageBean: ApiGrantPackageBean,
    var appName: String,
    var appIcon: Drawable?,
) : IApiGrantData

data class EmptyApiGrantDataBean(val msg: String) : IApiGrantData

interface IApiGrantData : BaseBean
