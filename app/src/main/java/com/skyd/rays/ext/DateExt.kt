package com.skyd.rays.ext

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Long.toDateTimeString(
    dateStyle: Int = SimpleDateFormat.MEDIUM,
    timeStyle: Int = SimpleDateFormat.MEDIUM,
    locale: Locale = Locale.getDefault()
): String = Date(this).toDateTimeString(dateStyle, timeStyle, locale)

fun Date.toDateTimeString(
    dateStyle: Int = SimpleDateFormat.MEDIUM,
    timeStyle: Int = SimpleDateFormat.MEDIUM,
    locale: Locale = Locale.getDefault()
): String = SimpleDateFormat
    .getDateTimeInstance(dateStyle, timeStyle, locale)
    .format(this)

fun Long.toDateTimeString(pattern: String): String = Date(this).toDateTimeString(pattern)

fun Date.toDateTimeString(pattern: String): String = runCatching {
    SimpleDateFormat(pattern, Locale.getDefault())
        .format(this)
}.getOrDefault("")