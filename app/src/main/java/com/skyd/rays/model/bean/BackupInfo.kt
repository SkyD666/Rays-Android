package com.skyd.rays.model.bean

import kotlinx.serialization.Serializable

@Serializable
data class BackupInfo(
    var uuid: String,
    var contentMd5: String,
    var stickerMd5: String? = null,
    var modifiedTime: Long,
    var isDeleted: Boolean = false
) : BaseBean
