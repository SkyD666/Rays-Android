package com.skyd.rays.util.share

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import com.skyd.rays.R
import com.skyd.rays.appContext
import com.skyd.rays.util.share.app.DiscordAppInfo
import com.skyd.rays.util.share.app.IAppInfo
import com.skyd.rays.util.share.app.InstagramAppInfo
import com.skyd.rays.util.share.app.QQAppInfo
import com.skyd.rays.util.share.app.TelegramAppInfo
import com.skyd.rays.util.share.app.WeChatAppInfo
import com.skyd.rays.util.share.app.WeiboAppInfo
import com.skyd.rays.util.share.app.WhatsAppAppInfo

object ShareUtil {
    private val apps by lazy {
        arrayOf(
            QQAppInfo(), WeChatAppInfo(), WeiboAppInfo(), TelegramAppInfo(), DiscordAppInfo(),
            WhatsAppAppInfo(), InstagramAppInfo()
        )
    }

    private fun isInstalled(packageName: String): Boolean {
        val packageInfo: PackageInfo? = try {
            appContext.packageManager.getPackageInfo(packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
        return packageInfo != null
    }

    fun share(context: Context, uris: List<Uri>, topActivityFullName: String) {
        if (topActivityFullName.isBlank()) {
            startShare(context, uris)
            return
        }
        apps.forEach {
            if (share(context, topActivityFullName, uris, it)) {
                return
            }
        }
        startShare(context, uris)
    }

    fun share(context: Context, topActivityFullName: String, uris: List<Uri>, appInfo: IAppInfo): Boolean {
        if (!isInstalled(appInfo.packageName)) return false
        return appInfo.share(context, topActivityFullName, uris)
    }

    fun startShare(
        context: Context,
        uris: List<Uri>,
        packageName: String? = null,
        className: String? = null,
    ) {
        Log.i("startShare", "$packageName $className")
        val shareIntent: Intent = Intent().apply {
            if (uris.size == 1) {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, uris.first())
            } else {
                action = Intent.ACTION_SEND_MULTIPLE
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
            }
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
            type = "image/*"
            if (!packageName.isNullOrBlank() && !className.isNullOrBlank()) {
                setClassName(packageName, className)
            } else if (!packageName.isNullOrBlank()) {
                setPackage(packageName)
            }
        }
        context.startActivity(
            Intent.createChooser(
                shareIntent,
                context.resources.getText(R.string.send_sticker)
            )
        )
    }
}
