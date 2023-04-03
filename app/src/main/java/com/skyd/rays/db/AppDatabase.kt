package com.skyd.rays.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import com.skyd.rays.db.dao.StickerDao
import com.skyd.rays.db.dao.SearchDomainDao
import com.skyd.rays.db.dao.TagDao
import com.skyd.rays.model.bean.StickerBean
import com.skyd.rays.model.bean.SearchDomainBean
import com.skyd.rays.model.bean.TagBean

const val APP_DATA_BASE_FILE_NAME = "app.db"

@Database(
    entities = [StickerBean::class, TagBean::class, SearchDomainBean::class], version = 1
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

        private val migrations = arrayOf<Migration>()

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
