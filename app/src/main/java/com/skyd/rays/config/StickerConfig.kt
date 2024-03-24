package com.skyd.rays.config

import com.skyd.rays.appContext
import java.io.File

val STICKER_DIR: String = File(appContext.filesDir.path, "Sticker").path

val TEMP_STICKER_DIR: File = File(appContext.cacheDir, "TempSticker")
val PROVIDER_THUMBNAIL_DIR: File = File(TEMP_STICKER_DIR, "Provider/Thumbnail")

val IMPORT_FILES_DIR: File = File(appContext.cacheDir, "ImportFiles")
val EXPORT_FILES_DIR: File = File(appContext.cacheDir, "ExportFiles")
