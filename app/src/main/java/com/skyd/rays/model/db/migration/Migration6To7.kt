package com.skyd.rays.model.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.skyd.rays.model.bean.MIME_TYPE_TABLE_NAME
import com.skyd.rays.model.bean.MimeTypeBean
import com.skyd.rays.model.bean.STICKER_TABLE_NAME
import com.skyd.rays.model.bean.StickerBean

class Migration6To7 : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
                CREATE TABLE $MIME_TYPE_TABLE_NAME (
                    ${MimeTypeBean.STICKER_UUID_COLUMN} TEXT PRIMARY KEY NOT NULL,
                    ${MimeTypeBean.STICKER_MD5_COLUMN} TEXT NOT NULL,
                    ${MimeTypeBean.MIME_TYPE_COLUMN} TEXT NOT NULL,
                    FOREIGN KEY (${MimeTypeBean.STICKER_UUID_COLUMN})
                                REFERENCES $STICKER_TABLE_NAME(${StickerBean.UUID_COLUMN})
                                ON DELETE CASCADE
                )
                """
        )
    }
}