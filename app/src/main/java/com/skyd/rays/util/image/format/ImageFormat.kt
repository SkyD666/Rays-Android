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

    companion object {
        fun fromMimeType(mimeType: String): ImageFormat {
            return when (mimeType) {
                "image/jpg" -> JPG
                "image/png" -> PNG
                "image/gif" -> GIF
                "image/webp" -> WEBP
                "image/bmp" -> BMP
                "image/heif" -> HEIF
                "image/heic" -> HEIC
                else -> UNDEFINED
            }
        }
    }
}