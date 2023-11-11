package com.skyd.rays.model.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.skyd.rays.model.bean.API_GRANT_PACKAGE_TABLE_NAME
import com.skyd.rays.model.bean.ApiGrantPackageBean

class Migration5To6 : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE $API_GRANT_PACKAGE_TABLE_NAME
                    (${ApiGrantPackageBean.PACKAGE_NAME_COLUMN} TEXT PRIMARY KEY NOT NULL,
                    ${ApiGrantPackageBean.ENABLED_COLUMN} INTEGER NOT NULL)"""
        )
    }
}