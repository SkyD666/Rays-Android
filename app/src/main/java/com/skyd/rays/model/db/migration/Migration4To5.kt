package com.skyd.rays.model.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.skyd.rays.model.bean.URI_STRING_SHARE_PACKAGE_TABLE_NAME
import com.skyd.rays.model.bean.UriStringSharePackageBean

class Migration4To5 : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE $URI_STRING_SHARE_PACKAGE_TABLE_NAME
                    (${UriStringSharePackageBean.PACKAGE_NAME_COLUMN} TEXT PRIMARY KEY NOT NULL,
                    ${UriStringSharePackageBean.ENABLED_COLUMN} INTEGER NOT NULL)"""
        )
    }
}