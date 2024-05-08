package com.skyd.rays.util.share.app

import android.content.Context
import android.net.Uri
import com.skyd.rays.util.share.ShareUtil

class WhatsAppAppInfo : IAppInfo {
    override val packageName: String
        get() = "com.whatsapp"

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
            className = "com.whatsapp.contact.picker.ContactPicker"
        )
        return true
    }
}