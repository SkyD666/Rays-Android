package com.skyd.rays.util.share.app

import android.content.Context
import android.net.Uri
import com.skyd.rays.util.share.ShareUtil

class InstagramAppInfo : IAppInfo {
    override val packageName: String
        get() = "com.instagram.android"

    override fun share(context: Context, topActivityFullName: String, uris: List<Uri>): Boolean {
        if (!topActivityFullName.startsWith("com.instagram")) return false
        ShareUtil.startShare(
            context = context,
            uris = uris,
            packageName = packageName,
            className = ""
        )
        return true
    }
}