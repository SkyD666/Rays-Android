package com.skyd.rays.config

import com.skyd.rays.R
import com.skyd.rays.model.bean.STICKER_TABLE_NAME
import com.skyd.rays.model.bean.StickerBean.Companion.STICKER_MD5_COLUMN
import com.skyd.rays.model.bean.StickerBean.Companion.TITLE_COLUMN
import com.skyd.rays.model.bean.StickerBean.Companion.UUID_COLUMN
import com.skyd.rays.model.bean.TAG_TABLE_NAME
import com.skyd.rays.model.bean.TagBean.Companion.TAG_COLUMN

val allSearchDomain: HashMap<Pair<String, Int>, List<Pair<String, Int>>> = hashMapOf(
    (STICKER_TABLE_NAME to R.string.db_sticker_table) to listOf(
        UUID_COLUMN to R.string.db_sticker_table_uuid,
        TITLE_COLUMN to R.string.db_sticker_table_title,
        STICKER_MD5_COLUMN to R.string.db_sticker_table_md5,
    ),
    (TAG_TABLE_NAME to R.string.db_tag_table) to listOf(
        TAG_COLUMN to R.string.db_tag_table_tag,
    ),
)
