package com.skyd.rays.model.bean

import com.skyd.rays.R

enum class ShareSheetAction(
    val icon: Int,
    val label: Int
) {
    Copy(
        icon = R.drawable.ic_copy,
        label = R.string.share_sheet_action_copy
    ),
    Save(
        icon = R.drawable.ic_save,
        label = R.string.share_sheet_action_save
    )
}