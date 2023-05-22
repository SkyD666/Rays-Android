package com.skyd.rays.model.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.skyd.rays.model.bean.SearchDomainBean
import com.skyd.rays.model.bean.StickerBean
import com.skyd.rays.model.bean.TagBean
import com.skyd.rays.model.db.dao.SearchDomainDao
import com.skyd.rays.model.db.dao.StickerDao
import com.skyd.rays.model.db.dao.TagDao
import com.skyd.rays.model.db.migration.Migration1To2
import com.skyd.rays.model.db.migration.Migration2To3
import com.skyd.rays.model.db.migration.Migration3To4

const val APP_DATA_BASE_FILE_NAME = "app.db"

@Database(
    entities = [StickerBean::class, TagBean::class, SearchDomainBean::class], version = 4
)
@TypeConverters(
    value = []
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun stickerDao(): StickerDao
    abstract fun tagDao(): TagDao
    abstract fun searchDomainDao(): SearchDomainDao

    companion object {
        private var instance: AppDatabase? = null

        private val migrations = arrayOf(
            Migration1To2(), Migration2To3(), Migration3To4()
        )

        fun getInstance(context: Context): AppDatabase {
            return if (instance == null) {
                synchronized(this) {
                    Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        APP_DATA_BASE_FILE_NAME
                    )
                        .addMigrations(*migrations)
                        .build()
                }
            } else {
                instance as AppDatabase
            }

        }
    }
}
