package com.skyd.rays.ext

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun dateTime(timestamp: Long, pattern: String = "yyyy/MM/dd HH:mm:ss"): String {
    return try {
        SimpleDateFormat(pattern, Locale.getDefault())
            .format(Date(timestamp))
    } catch (e: Exception) {
        ""
    }
}