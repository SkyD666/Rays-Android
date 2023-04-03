package com.skyd.rays.ext

import com.github.stuxuhai.jpinyin.PinyinFormat
import com.github.stuxuhai.jpinyin.PinyinHelper

fun CharSequence.startWithBlank(): Boolean = matches("^\\s+.*".toRegex())

fun String.toPinyin(): String =
    PinyinHelper.convertToPinyinString(this, " ", PinyinFormat.WITH_TONE_MARK)

fun ignoreCaseOpt(ignoreCase: Boolean) =
    if (ignoreCase) setOf(RegexOption.IGNORE_CASE) else emptySet()

fun String.indexesOf(pat: String, ignoreCase: Boolean = true): List<Int> =
    pat.toRegex(ignoreCaseOpt(ignoreCase))
        .findAll(this)
        .map { it.range.first }
        .toList()
