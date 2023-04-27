package com.skyd.rays.di

import android.content.Context
import com.skyd.rays.model.db.AppDatabase
import com.skyd.rays.model.db.dao.StickerDao
import com.skyd.rays.model.db.dao.SearchDomainDao
import com.skyd.rays.model.db.dao.TagDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        AppDatabase.getInstance(context)

    @Provides
    @Singleton
    fun provideStickerDao(database: AppDatabase): StickerDao = database.stickerDao()

    @Provides
    @Singleton
    fun provideTagDao(database: AppDatabase): TagDao = database.tagDao()

    @Provides
    @Singleton
    fun provideGroupDao(database: AppDatabase): SearchDomainDao = database.searchDomainDao()
}
