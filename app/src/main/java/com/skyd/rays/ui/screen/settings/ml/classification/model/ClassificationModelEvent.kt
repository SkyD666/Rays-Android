package com.skyd.rays.ui.screen.settings.ml.classification.model

import android.net.Uri
import com.skyd.rays.base.IUiEvent

class ClassificationModelEvent(
    val deleteUiEvent: DeleteUiEvent? = null,
    val importUiEvent: ImportUiEvent? = null,
) : IUiEvent

sealed class DeleteUiEvent {
    class Success(val path: String) : DeleteUiEvent()
}

sealed class ImportUiEvent {
    class Success(val uri: Uri) : ImportUiEvent()
}