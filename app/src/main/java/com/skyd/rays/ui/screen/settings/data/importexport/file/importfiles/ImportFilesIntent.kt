package com.skyd.rays.ui.screen.settings.data.importexport.file.importfiles

import android.net.Uri
import com.skyd.rays.base.mvi.MviIntent
import com.skyd.rays.model.db.dao.sticker.HandleImportedStickerProxy

sealed interface ImportFilesIntent : MviIntent {
    data object Init : ImportFilesIntent
    data class Import(
        val backupFileUri: Uri,
        val proxy: HandleImportedStickerProxy,
    ) : ImportFilesIntent
}