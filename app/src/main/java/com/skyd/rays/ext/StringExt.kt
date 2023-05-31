package com.skyd.rays.ext

import android.graphics.Typeface
import android.text.Html
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.text.style.BulletSpan
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.SubscriptSpan
import android.text.style.SuperscriptSpan
import android.text.style.TypefaceSpan
import android.text.style.UnderlineSpan
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.em

fun CharSequence.startWithBlank(): Boolean = matches("^\\s+.*".toRegex())

fun ignoreCaseOpt(ignoreCase: Boolean) =
    if (ignoreCase) setOf(RegexOption.IGNORE_CASE) else emptySet()

fun String.indexesOf(pat: String, ignoreCase: Boolean = true): List<Int> =
    pat.toRegex(ignoreCaseOpt(ignoreCase))
        .findAll(this)
        .map { it.range.first }
        .toList()

fun String.readable(): CharSequence = spannableStringToAnnotatedString(
    text = Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY)
)

private fun spannableStringToAnnotatedString(
    text: CharSequence,
): CharSequence {
    return if (text is Spanned) {
        buildAnnotatedString {
            append((text.toString()))
            text.getSpans(0, text.length, Any::class.java).forEach {
                val start = text.getSpanStart(it)
                val end = text.getSpanEnd(it)
                when (it) {
                    is StyleSpan -> when (it.style) {
                        Typeface.NORMAL -> addStyle(
                            SpanStyle(
                                fontWeight = FontWeight.Normal,
                                fontStyle = FontStyle.Normal
                            ),
                            start,
                            end
                        )

                        Typeface.BOLD -> addStyle(
                            SpanStyle(
                                fontWeight = FontWeight.Bold,
                                fontStyle = FontStyle.Normal
                            ),
                            start,
                            end
                        )

                        Typeface.ITALIC -> addStyle(
                            SpanStyle(
                                fontWeight = FontWeight.Normal,
                                fontStyle = FontStyle.Italic
                            ),
                            start,
                            end
                        )

                        Typeface.BOLD_ITALIC -> addStyle(
                            SpanStyle(
                                fontWeight = FontWeight.Bold,
                                fontStyle = FontStyle.Italic
                            ),
                            start,
                            end
                        )
                    }

                    is TypefaceSpan -> addStyle(
                        SpanStyle(
                            fontFamily = when (it.family) {
                                FontFamily.SansSerif.name -> FontFamily.SansSerif
                                FontFamily.Serif.name -> FontFamily.Serif
                                FontFamily.Monospace.name -> FontFamily.Monospace
                                FontFamily.Cursive.name -> FontFamily.Cursive
                                else -> FontFamily.Default
                            }
                        ),
                        start,
                        end
                    )

                    is BulletSpan -> {
                        Log.d("StringResources", "BulletSpan not supported yet")
                        addStyle(SpanStyle(), start, end)
                    }

                    is AbsoluteSizeSpan -> {
                        Log.d("StringResources", "AbsoluteSizeSpan not supported yet")
                        addStyle(SpanStyle(), start, end)
                    }

                    is RelativeSizeSpan -> addStyle(
                        SpanStyle(fontSize = it.sizeChange.em),
                        start,
                        end
                    )

                    is StrikethroughSpan -> addStyle(
                        SpanStyle(textDecoration = TextDecoration.LineThrough),
                        start,
                        end
                    )

                    is UnderlineSpan -> addStyle(
                        SpanStyle(textDecoration = TextDecoration.Underline),
                        start,
                        end
                    )

                    is SuperscriptSpan -> addStyle(
                        SpanStyle(baselineShift = BaselineShift.Superscript),
                        start,
                        end
                    )

                    is SubscriptSpan -> addStyle(
                        SpanStyle(baselineShift = BaselineShift.Subscript),
                        start,
                        end
                    )

                    is ForegroundColorSpan -> addStyle(
                        SpanStyle(color = Color(it.foregroundColor)),
                        start,
                        end
                    )

                    else -> addStyle(SpanStyle(), start, end)
                }
            }
        }
    } else {
        AnnotatedString(text.toString())
    }
}