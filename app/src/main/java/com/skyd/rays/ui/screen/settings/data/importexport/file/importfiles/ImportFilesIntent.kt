package com.skyd.rays.ui.screen.settings.data.importexport.file.importfiles

import android.net.Uri
import com.skyd.rays.base.IUiIntent
import com.skyd.rays.model.db.dao.sticker.HandleImportedStickerProxy

sealed class ImportFilesIntent : IUiIntent {
    data class Import(
        val backupFileUri: Uri,
        val proxy: HandleImportedStickerProxy,
    ) : ImportFilesIntent()
}