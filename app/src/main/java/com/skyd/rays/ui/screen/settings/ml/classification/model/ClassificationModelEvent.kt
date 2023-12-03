package com.skyd.rays.ui.screen.settings.ml.classification.model

import android.net.Uri
import com.skyd.rays.base.mvi.MviSingleEvent
import com.skyd.rays.model.bean.ModelBean

sealed interface ClassificationModelEvent : MviSingleEvent {
    sealed interface DeleteEvent : ClassificationModelEvent {
        data class Success(val deletedUri: Uri) : DeleteEvent
        data class Failed(val msg: String) : DeleteEvent
    }

    sealed interface ImportEvent : ClassificationModelEvent {
        data class Success(val newModel: ModelBean) : ImportEvent
        data class Failed(val msg: String) : ImportEvent
    }
}