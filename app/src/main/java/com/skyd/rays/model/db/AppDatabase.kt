package com.skyd.rays.model.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.skyd.rays.model.bean.ApiGrantPackageBean
import com.skyd.rays.model.bean.MimeTypeBean
import com.skyd.rays.model.bean.SearchDomainBean
import com.skyd.rays.model.bean.StickerBean
import com.skyd.rays.model.bean.StickerShareTimeBean
import com.skyd.rays.model.bean.TagBean
import com.skyd.rays.model.bean.UriStringSharePackageBean
import com.skyd.rays.model.db.dao.ApiGrantPackageDao
import com.skyd.rays.model.db.dao.SearchDomainDao
import com.skyd.rays.model.db.dao.TagDao
import com.skyd.rays.model.db.dao.UriStringSharePackageDao
import com.skyd.rays.model.db.dao.cache.StickerShareTimeDao
import com.skyd.rays.model.db.dao.sticker.MimeTypeDao
import com.skyd.rays.model.db.dao.sticker.StickerDao
import com.skyd.rays.model.db.migration.Migration1To2
import com.skyd.rays.model.db.migration.Migration2To3
import com.skyd.rays.model.db.migration.Migration3To4
import com.skyd.rays.model.db.migration.Migration4To5
import com.skyd.rays.model.db.migration.Migration5To6
import com.skyd.rays.model.db.migration.Migration6To7
import com.skyd.rays.model.db.migration.Migration7To8

const val APP_DATA_BASE_FILE_NAME = "app.db"

@Database(
    entities = [
        StickerBean::class,
        TagBean::class,
        SearchDomainBean::class,
        UriStringSharePackageBean::class,
        ApiGrantPackageBean::class,
        MimeTypeBean::class,
        StickerShareTimeBean::class,
    ],
    version = 8
)
@TypeConverters(
    value = []
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun stickerDao(): StickerDao
    abstract fun tagDao(): TagDao
    abstract fun searchDomainDao(): SearchDomainDao
    abstract fun uriStringSharePackageDao(): UriStringSharePackageDao
    abstract fun apiGrantPackageDao(): ApiGrantPackageDao
    abstract fun mimeTypeDao(): MimeTypeDao
    abstract fun stickerShareTimeDao(): StickerShareTimeDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        private val migrations = arrayOf(
            Migration1To2(), Migration2To3(), Migration3To4(), Migration4To5(), Migration5To6(),
            Migration6To7(), Migration7To8(),
        )

        fun getInstance(context: Context): AppDatabase {
            return if (instance == null) {
                synchronized(this) {
                    if (instance == null) {
                        Room.databaseBuilder(
                            context.applicationContext,
                            AppDatabase::class.java,
                            APP_DATA_BASE_FILE_NAME
                        )
                            .addMigrations(*migrations)
                            .build()
                            .apply { instance = this }
                    } else {
                        instance as AppDatabase
                    }
                }
            } else {
                instance as AppDatabase
            }

        }
    }
}
