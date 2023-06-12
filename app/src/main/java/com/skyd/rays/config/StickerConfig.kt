package com.skyd.rays.config

import com.skyd.rays.appContext
import kotlinx.coroutines.flow.MutableSharedFlow
import java.io.File

val STICKER_DIR: String = File(appContext.filesDir.path, "Sticker").path

val TEMP_STICKER_DIR: File = File(appContext.cacheDir, "TempSticker")

val refreshStickerData: MutableSharedFlow<Unit> =
    MutableSharedFlow(replay = 1, extraBufferCapacity = 1)