package com.skyd.rays.model.respository

import android.content.pm.PackageManager
import com.skyd.rays.R
import com.skyd.rays.appContext
import com.skyd.rays.base.BaseData
import com.skyd.rays.base.BaseRepository
import com.skyd.rays.model.bean.EmptyUriStringShareDataBean
import com.skyd.rays.model.bean.IUriStringShareData
import com.skyd.rays.model.bean.UriStringShareDataBean
import com.skyd.rays.model.bean.UriStringSharePackageBean
import com.skyd.rays.model.db.dao.UriStringSharePackageDao
import com.skyd.rays.util.CommonUtil.getAppInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class UriStringShareRepository @Inject constructor(
    private val uriStringSharePackageDao: UriStringSharePackageDao
) : BaseRepository() {
    suspend fun requestUpdate(bean: UriStringSharePackageBean): Flow<BaseData<IUriStringShareData>> {
        return flow {
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
            emitBaseData(BaseData<IUriStringShareData>().apply {
                this.code = 0
                this.data = data
            })
        }
    }

    suspend fun requestDelete(packageName: String): Flow<BaseData<Int>> {
        return flow {
            emitBaseData(BaseData<Int>().apply {
                this.code = 0
                this.data = uriStringSharePackageDao.deletePackage(pkgName = packageName)
            })
        }
    }

    suspend fun requestAllPackages(): Flow<BaseData<List<UriStringShareDataBean>>> {
        return flow {
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
            emitBaseData(BaseData<List<UriStringShareDataBean>>().apply {
                code = 0
                this.data = data
            })
        }
    }
}