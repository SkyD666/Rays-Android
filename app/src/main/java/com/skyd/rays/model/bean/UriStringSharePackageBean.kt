package com.skyd.rays.model.bean

import android.graphics.drawable.Drawable
import androidx.room.ColumnInfo
import androidx.room.Entity
import com.skyd.rays.base.BaseBean
import kotlinx.serialization.Serializable

const val URI_STRING_SHARE_PACKAGE_TABLE_NAME = "UriStringSharePackage"

@Serializable
@Entity(
    tableName = URI_STRING_SHARE_PACKAGE_TABLE_NAME,
    primaryKeys = [UriStringSharePackageBean.PACKAGE_NAME_COLUMN]
)
data class UriStringSharePackageBean(
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

data class UriStringShareDataBean(
    var uriStringSharePackageBean: UriStringSharePackageBean,
    var appName: String,
    var appIcon: Drawable?,
) : IUriStringShareData

data class EmptyUriStringShareDataBean(val msg: String) : IUriStringShareData

sealed interface IUriStringShareData : BaseBean
