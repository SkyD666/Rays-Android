package com.skyd.rays.util.share.app

import android.content.Context
import android.net.Uri

interface IAppInfo {
    val packageName: String

    fun share(context: Context, topActivityFullName: String, uris: List<Uri>): Boolean
}