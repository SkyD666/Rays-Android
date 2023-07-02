package com.skyd.rays.util.share.app

import android.content.Context
import android.net.Uri
import com.skyd.rays.util.share.ShareUtil

class QQAppInfo : IAppInfo {
    override val packageName: String
        get() = "com.tencent.mobileqq"

    override fun share(context: Context, topActivityFullName: String, uri: Uri): Boolean {
        if (!topActivityFullName.startsWith(packageName)) return false
        ShareUtil.startShare(
            context = context,
            uri = uri,
            packageName = packageName,
            className = "com.tencent.mobileqq.activity.JumpActivity"
        )
        return true
    }
}