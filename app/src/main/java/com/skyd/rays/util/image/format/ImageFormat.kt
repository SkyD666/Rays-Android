package com.skyd.rays.util.image.format

enum class ImageFormat {
    JPG,
    PNG,
    GIF,
    WEBP,
    BMP,
    UNDEFINED;

    override fun toString(): String {
        return when (this) {
            JPG -> ".jpg"
            PNG -> ".png"
            GIF -> ".gif"
            WEBP -> ".webp"
            BMP -> ".bmp"
            UNDEFINED -> ""
        }
    }
}