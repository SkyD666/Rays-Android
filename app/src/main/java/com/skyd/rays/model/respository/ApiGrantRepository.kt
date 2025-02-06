package com.skyd.rays.model.respository

import android.content.pm.PackageManager
import com.skyd.rays.R
import com.skyd.rays.appContext
import com.skyd.rays.base.BaseRepository
import com.skyd.rays.model.bean.ApiGrantDataBean
import com.skyd.rays.model.bean.ApiGrantPackageBean
import com.skyd.rays.model.bean.EmptyApiGrantDataBean
import com.skyd.rays.model.bean.IApiGrantData
import com.skyd.rays.model.db.dao.ApiGrantPackageDao
import com.skyd.rays.util.CommonUtil.getAppInfo
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ApiGrantRepository @Inject constructor(
    private val apiGrantPackageDao: ApiGrantPackageDao
) : BaseRepository() {
    fun requestUpdate(bean: ApiGrantPackageBean): Flow<IApiGrantData> {
        return flowOnIo {
            val pm: PackageManager = appContext.packageManager
            val data = runCatching {
                val info = getAppInfo(pm = pm, packageName = bean.packageName)
                apiGrantPackageDao.updatePackage(bean)
                ApiGrantDataBean(
                    apiGrantPackageBean = bean,
                    appName = info.applicationInfo?.loadLabel(pm).toString(),
                    appIcon = info.applicationInfo?.loadIcon(pm)
                )
            }.onFailure {
                it.printStackTrace()
            }.getOrNull() ?: EmptyApiGrantDataBean(
                msg = appContext.getString(
                    R.string.api_grant_repo_no_package,
                    bean.packageName
                )
            )
            emit(data)
        }
    }

    fun requestDelete(packageName: String): Flow<Pair<String, Int>> {
        return flowOnIo {
            emit(packageName to apiGrantPackageDao.deletePackage(pkgName = packageName))
        }
    }

    fun requestAllPackages(): Flow<List<ApiGrantDataBean>> {
        return flowOnIo {
            val pm: PackageManager = appContext.packageManager
            val data = apiGrantPackageDao.getAllPackage().mapNotNull { bean ->
                runCatching {
                    val info = getAppInfo(pm = pm, packageName = bean.packageName)
                    ApiGrantDataBean(
                        apiGrantPackageBean = bean,
                        appName = info.applicationInfo?.loadLabel(pm).toString(),
                        appIcon = info.applicationInfo?.loadIcon(pm)
                    )
                }.onFailure {
                    it.printStackTrace()
                    if (it is PackageManager.NameNotFoundException) {
                        apiGrantPackageDao.deletePackage(pkgName = bean.packageName)
                    }
                }.getOrNull()
            }
            emit(data)
        }
    }
}