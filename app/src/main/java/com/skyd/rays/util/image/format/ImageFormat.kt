package com.skyd.rays.util.image.format

enum class ImageFormat {
    JPG,
    PNG,
    GIF,
    WEBP,
    BMP,
    HEIF,
    HEIC,
    UNDEFINED;

    override fun toString(): String {
        return when (this) {
            JPG -> ".jpg"
            PNG -> ".png"
            GIF -> ".gif"
            WEBP -> ".webp"
            BMP -> ".bmp"
            HEIF -> ".heif"
            HEIC -> ".heic"
            UNDEFINED -> ""
        }
    }

    fun toMimeType(): String {
        return when (this) {
            JPG -> "image/jpg"
            PNG -> "image/png"
            GIF -> "image/gif"
            WEBP -> "image/webp"
            BMP -> "image/bmp"
            HEIF -> "image/heif"
            HEIC -> "image/heic"
            UNDEFINED -> "image/*"
        }
    }
}