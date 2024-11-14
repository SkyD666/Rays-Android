package com.skyd.rays.util.coil.apng

import coil3.ImageLoader
import coil3.asImage
import coil3.decode.DecodeResult
import coil3.decode.Decoder
import coil3.decode.ImageSource
import coil3.fetch.SourceFetchResult
import coil3.request.Options
import com.github.penfeizhou.animation.apng.APNGDrawable
import com.github.penfeizhou.animation.loader.StreamLoader
import com.skyd.rays.util.image.format.FormatStandard.PngFormat.PNG_FORMAT_DATA
import okio.ByteString.Companion.encodeUtf8
import okio.ByteString.Companion.toByteString
import java.io.ByteArrayInputStream

class AnimatedPngDecoder(private val source: ImageSource) : Decoder {

    override suspend fun decode(): DecodeResult {
        val inputStream = ByteArrayInputStream(source.source().readByteArray())
        val drawable = APNGDrawable(object : StreamLoader() {
            override fun getInputStream() = inputStream
        })
        drawable.setAutoPlay(false)
        drawable.start()
        drawable.setLoopLimit(-1)
        return DecodeResult(
            image = drawable.asImage(),
            isSampled = false
        )
    }

    class Factory : Decoder.Factory {
        /**
         * https://en.wikipedia.org/wiki/APNG
         * https://parsiya.net/blog/2018-02-25-extracting-png-chunks-with-go/
         * https://twitter.com/angealbertini/status/1372082666632327169
         */
        override fun create(
            result: SourceFetchResult,
            options: Options,
            imageLoader: ImageLoader
        ): Decoder? {
            if (result.mimeType == "image/apng") {
                return AnimatedPngDecoder(result.source)
            }

            val source = result.source
            // 检查是不是 PNG，如果不是的话，返回 null
            if (!source.source().rangeEquals(0, PNG_FORMAT_DATA.toByteString())) {
                return null
            }
            // 获取 IHDR 长度，其实应该都是十进制的 13
            var ihdrLength = 0
            source.source().apply {
                for (i in 8L..11L) {
                    if (!request(i + 1)) return null
                    ihdrLength = ihdrLength shl 8 or buffer[i].toInt()
                }
            }

            /**
             * 8：PNG格式
             * 4：4字节存储下一部分（IHDR）的长度
             * 4：Chunk Type（IHDR）
             * 13：IHDR length（ihdrLength）
             * 4：CRC32
             * 4：4字节存储下一部分（acTL）的长度
             */
            return if (source.source().rangeEquals(
                    offset = 8 + 4 + 4 + /*13*/ihdrLength.toLong() + 4 + 4,
                    bytes = "acTL".encodeUtf8(),
                )
            ) {
                AnimatedPngDecoder(result.source)
            } else {
                null
            }
        }
    }
}