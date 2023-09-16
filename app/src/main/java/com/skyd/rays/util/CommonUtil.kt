package com.skyd.rays.util

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import com.skyd.rays.R
import com.skyd.rays.appContext
import com.skyd.rays.ui.component.showToast

object CommonUtil {
    fun openBrowser(url: String) {
        try {
            val uri: Uri = Uri.parse(url)
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            appContext.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
            appContext.getString(R.string.no_browser_found, url).showToast(Toast.LENGTH_LONG)
        }
    }

    fun getAppVersionName(): String {
        var appVersionName = ""
        try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                appContext.applicationContext
                    .packageManager
                    .getPackageInfo(appContext.packageName, PackageManager.PackageInfoFlags.of(0L))
            } else {
                appContext.applicationContext
                    .packageManager
                    .getPackageInfo(appContext.packageName, 0)
            }
            appVersionName = packageInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return appVersionName
    }

    fun getAppVersionCode(): Long {
        var appVersionCode: Long = 0
        try {
            val packageInfo = appContext.applicationContext
                .packageManager
                .getPackageInfo(appContext.packageName, 0)
            appVersionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                packageInfo.versionCode.toLong()
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return appVersionCode
    }

    fun getAppInfo(pm: PackageManager, packageName: String): PackageInfo {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.getPackageInfo(
                packageName,
                PackageManager.PackageInfoFlags
                    .of(PackageManager.MATCH_UNINSTALLED_PACKAGES.toLong())
            )
        } else {
            pm.getPackageInfo(packageName, PackageManager.MATCH_UNINSTALLED_PACKAGES)
        }
    }
}