package com.skyd.rays.config

import android.content.Context
import java.io.File

// https://github.com/SkyD666/Rays-Android/issues/23
val Context.STICKER_DIR: String
    get() = File(filesDir.path, "Sticker")
        .apply { if (!exists()) mkdirs() }
        .path

val Context.TEMP_STICKER_DIR: File
    get() = File(cacheDir, "TempSticker")
        .apply { if (!exists()) mkdirs() }
val Context.PROVIDER_THUMBNAIL_DIR: File
    get() = File(TEMP_STICKER_DIR, "Provider/Thumbnail")
        .apply { if (!exists()) mkdirs() }

val Context.IMPORT_FILES_DIR: File
    get() = File(cacheDir, "ImportFiles")
        .apply { if (!exists()) mkdirs() }
val Context.EXPORT_FILES_DIR: File
    get() = File(cacheDir, "ExportFiles")
        .apply { if (!exists()) mkdirs() }
