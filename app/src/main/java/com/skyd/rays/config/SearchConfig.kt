package com.skyd.rays.config

import com.skyd.rays.model.bean.STICKER_TABLE_NAME
import com.skyd.rays.model.bean.StickerBean.Companion.TITLE_COLUMN
import com.skyd.rays.model.bean.StickerBean.Companion.UUID_COLUMN
import com.skyd.rays.model.bean.TAG_TABLE_NAME
import com.skyd.rays.model.bean.TagBean.Companion.TAG_COLUMN

val allSearchDomain: HashMap<Pair<String, String>, List<Pair<String, String>>> = hashMapOf(
    (STICKER_TABLE_NAME to "段落表") to listOf(
        UUID_COLUMN to "UUID",
        TITLE_COLUMN to "标题",
    ),
    (TAG_TABLE_NAME to "标签表") to listOf(
        TAG_COLUMN to "标签",
    ),
)
