package com.skyd.rays.ext

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.net.Uri
import androidx.fragment.app.FragmentActivity


val Context.activity: Activity
    get() {
        var ctx = this
        while (ctx is ContextWrapper) {
            if (ctx is Activity) {
                return ctx
            }
            ctx = ctx.baseContext
        }
        error("can't find activity: $this")
    }

val Context.fragmentActivity: FragmentActivity?
    get() {
        return this.activity as? FragmentActivity
    }

val Context.screenIsLand: Boolean
    get() = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

val Configuration.screenIsLand: Boolean
    get() = orientation == Configuration.ORIENTATION_LANDSCAPE

fun Context.checkUriReadPermission(uri: Uri?): Boolean {
    uri ?: return false
    val contentResolver: ContentResolver = contentResolver
    return try {
        contentResolver.query(uri, null, null, null, null).use { }
        true
    } catch (e: SecurityException) {
        false
    }
}