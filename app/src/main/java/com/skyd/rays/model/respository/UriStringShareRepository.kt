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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class UriStringShareRepository(
    private val uriStringSharePackageDao: UriStringSharePackageDao
) : BaseRepository() {
    fun requestUpdate(bean: UriStringSharePackageBean): Flow<IUriStringShareData> = flow {
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
    }.flowOn(Dispatchers.IO)

    fun requestDelete(packageName: String): Flow<Pair<String, Int>> = flow {
        emit(packageName to uriStringSharePackageDao.deletePackage(pkgName = packageName))
    }.flowOn(Dispatchers.IO)

    fun requestAllPackages(): Flow<List<UriStringShareDataBean>> = flow {
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
    }.flowOn(Dispatchers.IO)
}