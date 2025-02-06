package com.skyd.rays.model.respository

import android.content.pm.PackageManager
import com.skyd.rays.R
import com.skyd.rays.appContext
import com.skyd.rays.base.BaseRepository
import com.skyd.rays.model.bean.EmptyUriStringShareDataBean
import com.skyd.rays.model.bean.IUriStringShareData
import com.skyd.rays.model.bean.UriStringShareDataBean
import com.skyd.rays.model.bean.UriStringSharePackageBean
import com.skyd.rays.model.db.dao.UriStringSharePackageDao
import com.skyd.rays.util.CommonUtil.getAppInfo
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UriStringShareRepository @Inject constructor(
    private val uriStringSharePackageDao: UriStringSharePackageDao
) : BaseRepository() {
    fun requestUpdate(bean: UriStringSharePackageBean): Flow<IUriStringShareData> {
        return flowOnIo {
            val pm: PackageManager = appContext.packageManager
            val data = runCatching {
                val info = getAppInfo(pm = pm, packageName = bean.packageName)
                uriStringSharePackageDao.updatePackage(bean)
                UriStringShareDataBean(
                    uriStringSharePackageBean = bean,
                    appName = info.applicationInfo?.loadLabel(pm).toString(),
                    appIcon = info.applicationInfo?.loadIcon(pm)
                )
            }.onFailure {
                it.printStackTrace()
            }.getOrNull() ?: EmptyUriStringShareDataBean(
                msg = appContext.getString(
                    R.string.uri_string_share_repo_no_package,
                    bean.packageName
                )
            )
            emit(data)
        }
    }

    fun requestDelete(packageName: String): Flow<Pair<String, Int>> {
        return flowOnIo {
            emit(packageName to uriStringSharePackageDao.deletePackage(pkgName = packageName))
        }
    }

    fun requestAllPackages(): Flow<List<UriStringShareDataBean>> {
        return flowOnIo {
            val pm: PackageManager = appContext.packageManager
            val data = uriStringSharePackageDao.getAllPackage().mapNotNull { bean ->
                runCatching {
                    val info = getAppInfo(pm = pm, packageName = bean.packageName)
                    UriStringShareDataBean(
                        uriStringSharePackageBean = bean,
                        appName = info.applicationInfo?.loadLabel(pm).toString(),
                        appIcon = info.applicationInfo?.loadIcon(pm)
                    )
                }.onFailure {
                    it.printStackTrace()
                    if (it is PackageManager.NameNotFoundException) {
                        uriStringSharePackageDao.deletePackage(pkgName = bean.packageName)
                    }
                }.getOrNull()
            }
            emit(data)
        }
    }
}