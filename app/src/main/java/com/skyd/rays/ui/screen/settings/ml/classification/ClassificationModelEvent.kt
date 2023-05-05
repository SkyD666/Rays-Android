package com.skyd.rays.ui.screen.settings.ml.classification

import android.net.Uri
import com.skyd.rays.base.IUiEvent

data class ClassificationModelEvent(
    val deleteUiEvent: DeleteUiEvent? = null,
    val importUiEvent: ImportUiEvent? = null,
) : IUiEvent

sealed class DeleteUiEvent {
    data class Success(val path: String) : DeleteUiEvent()
}

sealed class ImportUiEvent {
    data class Success(val uri: Uri) : ImportUiEvent()
}