package com.skyd.rays.model.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.skyd.rays.model.bean.STICKER_TABLE_NAME
import com.skyd.rays.model.bean.StickerBean

class Migration1To2 : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE $STICKER_TABLE_NAME ADD ${StickerBean.MODIFY_TIME_COLUMN} INTEGER")
    }
}