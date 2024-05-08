package com.skyd.rays.util.share.app

import android.content.Context
import android.net.Uri
import com.skyd.rays.util.share.ShareUtil

class WeiboAppInfo : IAppInfo {
    override val packageName: String
        get() = "com.sina.weibo"

    override fun share(
        context: Context,
        topActivityFullName: String,
        uris: List<Uri>,
        mimetype: String?,
    ): Boolean {
        if (!topActivityFullName.startsWith(packageName)) return false
        ShareUtil.startShare(
            context = context,
            uris = uris,
            mimetype = mimetype,
            packageName = packageName,
            className = "com.sina.weibo.composerinde.ComposerDispatchActivity"
        )
        return true
    }
}