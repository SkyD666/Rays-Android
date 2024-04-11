package com.skyd.rays.model.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.skyd.rays.model.bean.STICKER_SHARE_TIME_TABLE_NAME
import com.skyd.rays.model.bean.STICKER_TABLE_NAME
import com.skyd.rays.model.bean.StickerBean
import com.skyd.rays.model.bean.StickerShareTimeBean

class Migration7To8 : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
                CREATE TABLE $STICKER_SHARE_TIME_TABLE_NAME (
                    ${StickerShareTimeBean.STICKER_UUID_COLUMN} TEXT NOT NULL,
                    ${StickerShareTimeBean.SHARE_TIME_COLUMN} INTEGER NOT NULL,
                    PRIMARY KEY (${StickerShareTimeBean.STICKER_UUID_COLUMN}, ${StickerShareTimeBean.SHARE_TIME_COLUMN})
                    FOREIGN KEY (${StickerShareTimeBean.STICKER_UUID_COLUMN})
                                REFERENCES $STICKER_TABLE_NAME(${StickerBean.UUID_COLUMN})
                                ON DELETE CASCADE
                )
                """
        )
    }
}