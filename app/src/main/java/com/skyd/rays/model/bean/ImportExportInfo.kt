package com.skyd.rays.model.bean

import android.net.Uri
import android.os.Parcelable
import androidx.annotation.Keep
import com.skyd.rays.base.BaseBean
import kotlinx.parcelize.Parcelize

@Keep
sealed interface ImportExportInfo : BaseBean

@Parcelize
data class ImportExportResultInfo(
    var time: Long,
    var count: Int,
    var backupFile: Uri,
) : ImportExportInfo, Parcelable

@Parcelize
data class ImportExportWaitingInfo(
    var current: Int?,
    var total: Int?,
    var msg: String,
) : ImportExportInfo, Parcelable