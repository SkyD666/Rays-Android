package com.skyd.rays.util.share

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.service.chooser.ChooserAction
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ShareCompat
import com.skyd.rays.R
import com.skyd.rays.appContext
import com.skyd.rays.model.bean.ShareSheetAction
import com.skyd.rays.model.broadcast.ShareSheetActionsReceiver
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

    fun share(
        context: Context,
        topActivityFullName: String,
        uris: List<Uri>,
        appInfo: IAppInfo
    ): Boolean {
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
        val shareIntent = ShareCompat.IntentBuilder(context)
            .apply { uris.forEach { addStream(it) } }
            .setType("image/*")
            .setChooserTitle(R.string.send_sticker)
            .createChooserIntent()

        if (!packageName.isNullOrBlank() && !className.isNullOrBlank()) {
            shareIntent.setClassName(packageName, className)
        } else if (!packageName.isNullOrBlank()) {
            shareIntent.setPackage(packageName)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE && uris.size == 1) {
            shareIntent.appendActions(context, uris)
        }

        context.startActivity(shareIntent)
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun Intent.appendActions(
        context: Context,
        uris: List<Uri>
    ) {
        val actionIntents = ShareSheetAction.entries.map {
            val intent = Intent(context, ShareSheetActionsReceiver::class.java).apply {
                putExtra("action", it.ordinal)
                this.setData(uris.first())
            }

            ChooserAction
                .Builder(
                    Icon.createWithResource(context, it.icon),
                    context.getString(it.label),
                    PendingIntent.getBroadcast(
                        context, it.ordinal, intent,
                        PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                )
                .build()
        }.toTypedArray()
        putExtra(Intent.EXTRA_CHOOSER_CUSTOM_ACTIONS, actionIntents)
    }
}
