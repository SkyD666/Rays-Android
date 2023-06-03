package com.skyd.rays.util.image

import com.skyd.rays.util.image.format.FormatStandard.Companion.formatStandards
import com.skyd.rays.util.image.format.ImageFormat
import java.io.InputStream

object ImageFormatChecker {
    fun check(tested: InputStream): ImageFormat {
        formatStandards.forEach {
            if (it.check(tested)) {
                return it.format
            }
        }
        return ImageFormat.UNDEFINED
    }

    fun check(tested: ByteArray): ImageFormat {
        formatStandards.forEach {
            if (it.check(tested)) {
                return it.format
            }
        }
        return ImageFormat.UNDEFINED
    }
}