package com.skyd.rays.util.image.format

import com.skyd.rays.util.image.format.FormatStandardUtil.baseCheck
import java.io.InputStream


sealed class FormatStandard(
    val format: ImageFormat,
    val requiredByteArraySize: Int
) {
    companion object {
        val formatStandards by lazy {
            arrayOf(
                ApngFormat,     // Should be before PngFormat
                PngFormat,
                JpgFormat,
                GifFormat,
                BmpFormat,
                WebpFormat,
                HeifFormat,
                HeicFormat
            )
        }
    }

    abstract fun check(tested: ByteArray): Boolean

    fun check(tested: InputStream, readByteArray: ByteArray?): Pair<Boolean, ByteArray> {
        require(requiredByteArraySize > 0)
        val delta = requiredByteArraySize - (readByteArray?.size ?: 0)
        val buffer: ByteArray = if (delta > 0) {
            val newBuffer = ByteArray(delta)
            tested.read(newBuffer)
            if (readByteArray == null) {
                newBuffer
            } else {
                readByteArray + newBuffer
            }
        } else {
            // 当 requiredByteArraySize > 0 时，这里 readByteArray 一定不为 null
            readByteArray!!
        }
        return check(buffer) to buffer
    }

    data object ApngFormat : FormatStandard(
        format = ImageFormat.APNG,
        requiredByteArraySize = 41,
    ) {
        override fun check(tested: ByteArray): Boolean {
            // Return false if the image is not png
            if (tested.size < 12 || !PngFormat.check(tested)) {
                return false
            }
            // Get IHDR length, in fact it should be decimal 13
            var ihdrLength = 0
            for (i in 8..11) {
                ihdrLength = ihdrLength shl 8 or tested[i].toInt()
            }

            /**
             * 8: PNG format
             * 4: 4 bytes to store the length of the next part (IHDR)
             * 4: Chunk Type (IHDR)
             * 13: IHDR length (ihdrLength)
             * 4: CRC32
             * 4: 4 bytes to store the length of the next part (acTL)
             */
            val startIndex = 8 + 4 + 4 + /*13*/ ihdrLength + 4 + 4
            return baseCheck(
                byteArrayOf(0x61, 0x63, 0x54, 0x4C),
                tested.copyOfRange(startIndex, startIndex + 4)
            )
        }
    }

    data object PngFormat : FormatStandard(
        format = ImageFormat.PNG,
        requiredByteArraySize = 8,
    ) {
        val PNG_FORMAT_DATA = byteArrayOf(
            0x89.toByte(),
            0x50.toByte(),
            0x4E.toByte(),
            0x47.toByte(),
            0x0D.toByte(),
            0x0A.toByte(),
            0x1A.toByte(),
            0x0A.toByte(),
        )

        override fun check(tested: ByteArray): Boolean = baseCheck(
            standard = PNG_FORMAT_DATA,
            tested = tested,
        )
    }

    data object JpgFormat : FormatStandard(
        format = ImageFormat.JPG,
        requiredByteArraySize = 3,
    ) {
        override fun check(tested: ByteArray): Boolean = baseCheck(
            standard = byteArrayOf(
                0xFF.toByte(),
                0xD8.toByte(),
                0xFF.toByte(),
            ),
            tested = tested,
        )
    }


    data object GifFormat : FormatStandard(
        format = ImageFormat.GIF,
        requiredByteArraySize = 6,
    ) {
        override fun check(tested: ByteArray): Boolean = baseCheck(
            standard = byteArrayOf(
                // GIF87a
                0x47.toByte(),
                0x49.toByte(),
                0x46.toByte(),
                0x38.toByte(),
                0x37.toByte(),
                0x61.toByte(),
            ),
            tested = tested,
        ) or baseCheck(
            // GIF89a
            standard = byteArrayOf(
                0x47.toByte(),
                0x49.toByte(),
                0x46.toByte(),
                0x38.toByte(),
                0x39.toByte(),
                0x61.toByte(),
            ),
            tested = tested,
        )
    }

    data object BmpFormat : FormatStandard(
        format = ImageFormat.BMP,
        requiredByteArraySize = 28,
    ) {
        /**
         * Offset: 00
         * 用于标识BMP和DIB文件的魔数，一般为0x42 0x4D，即ASCII的BM。
         * 以下为可能的取值：
         * BM – Windows 3.1x, 95, NT, ... etc.
         * BA – OS/2 struct Bitmap Array
         * CI – OS/2 struct Color Icon
         * CP – OS/2 const Color Pointer
         * IC – OS/2 struct Icon
         * PT – OS/2 Pointer
         */
        private val firstFieldArray = arrayOf(
            byteArrayOf(0x42.toByte(), 0x4D.toByte()),
            byteArrayOf(0x42.toByte(), 0x41.toByte()),
            byteArrayOf(0x43.toByte(), 0x49.toByte()),
            byteArrayOf(0x43.toByte(), 0x50.toByte()),
            byteArrayOf(0x49.toByte(), 0x43.toByte()),
            byteArrayOf(0x50.toByte(), 0x54.toByte()),
        )

        override fun check(tested: ByteArray): Boolean {
            var firstFieldResult = false
            for (firstField in firstFieldArray) {
                var r = false
                for (i in firstField.indices) {
                    if (firstField[i] != tested[i]) {
                        continue
                    } else if (i == 1) {
                        r = true
                    }
                }
                firstFieldResult = r
                if (r) {
                    break
                }
            }
            return firstFieldResult &&
                    // Offset: 1A == 0x01 && Offset: 1B == 0x00
                    tested[26] == 0x01.toByte() &&
                    tested[27] == 0x00.toByte()
        }
    }

    data object WebpFormat : FormatStandard(
        format = ImageFormat.WEBP,
        requiredByteArraySize = 12,
    ) {
        private val standard = byteArrayOf(
            0x52.toByte(),
            0x49.toByte(),
            0x46.toByte(),
            0x46.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x57.toByte(),
            0x45.toByte(),
            0x42.toByte(),
            0x50.toByte(),
        )

        override fun check(tested: ByteArray): Boolean {
            if (standard.size > tested.size) {
                return false
            }

            for (i in standard.indices) {
                if (standard[i] == 0x00.toByte()) {
                    continue
                } else if (standard[i] != tested[i]) {
                    return false
                }
            }

            return true
        }
    }

    /**
     * https://github.com/nokiatech/heif/issues/74
     * https://zh.wikipedia.org/wiki/%E9%AB%98%E6%95%88%E7%8E%87%E5%9B%BE%E5%83%8F%E6%96%87%E4%BB%B6%E6%A0%BC%E5%BC%8F#
     */
    data object HeifFormat : FormatStandard(
        format = ImageFormat.HEIF,
        requiredByteArraySize = 12,
    ) {
        private val ftyp = byteArrayOf(0x66.toByte(), 0x74.toByte(), 0x79.toByte(), 0x70.toByte())
        private val mif1 = byteArrayOf(0x6d.toByte(), 0x69.toByte(), 0x66.toByte(), 0x31.toByte())
        private val msf1 = byteArrayOf(0x6d.toByte(), 0x73.toByte(), 0x66.toByte(), 0x31.toByte())
        private val m = arrayOf(mif1, msf1)

        override fun check(tested: ByteArray): Boolean {
            for (i in 4..7) {
                if (tested[i] != ftyp[i - 4]) return false
            }

            m.forEach {
                if (baseCheck(it, tested.copyOfRange(8, 12))) {
                    return true
                }
            }
            return false
        }
    }

    data object HeicFormat : FormatStandard(
        format = ImageFormat.HEIC,
        requiredByteArraySize = 12,
    ) {
        private val ftyp = byteArrayOf(0x66.toByte(), 0x74.toByte(), 0x79.toByte(), 0x70.toByte())
        private val he = byteArrayOf(0x68.toByte(), 0x65.toByte())
        private val icIxIsVcVx = arrayOf(
            byteArrayOf(0x69.toByte(), 0x63.toByte()),
            byteArrayOf(0x69.toByte(), 0x78.toByte()),
            byteArrayOf(0x69.toByte(), 0x73.toByte()),
            byteArrayOf(0x76.toByte(), 0x63.toByte()),
            byteArrayOf(0x76.toByte(), 0x78.toByte()),
        )

        override fun check(tested: ByteArray): Boolean {
            for (i in 4..7) {
                if (tested[i] != ftyp[i - 4]) return false
            }
            for (i in 8..9) {
                if (tested[i] != he[i - 8]) return false
            }
            icIxIsVcVx.forEach {
                if (baseCheck(it, tested.copyOfRange(10, 12))) {
                    return true
                }
            }
            return false
        }
    }
}

object FormatStandardUtil {
    internal fun baseCheck(standard: ByteArray, tested: ByteArray): Boolean {
        if (standard.size > tested.size) {
            return false
        }

        for (i in standard.indices) {
            if (standard[i] != tested[i]) {
                return false
            }
        }

        return true
    }
}