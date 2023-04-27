package com.skyd.rays.ext

import android.text.Html

fun CharSequence.startWithBlank(): Boolean = matches("^\\s+.*".toRegex())

fun ignoreCaseOpt(ignoreCase: Boolean) =
    if (ignoreCase) setOf(RegexOption.IGNORE_CASE) else emptySet()

fun String.indexesOf(pat: String, ignoreCase: Boolean = true): List<Int> =
    pat.toRegex(ignoreCaseOpt(ignoreCase))
        .findAll(this)
        .map { it.range.first }
        .toList()

fun String.readable(): String = Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY).toString()
