package com.skyd.rays.di

import com.skyd.rays.model.db.AppDatabase
import com.skyd.rays.model.db.objectbox.ObjectBox
import org.koin.dsl.module

val databaseModule = module {
    single { ObjectBox.getInstance(get()) }
    single { AppDatabase.getInstance(get()) }
    single { get<AppDatabase>().stickerDao() }
    single { get<AppDatabase>().tagDao() }
    single { get<AppDatabase>().searchDomainDao() }
    single { get<AppDatabase>().uriStringSharePackageDao() }
    single { get<AppDatabase>().apiGrantPackageDao() }
    single { get<AppDatabase>().mimeTypeDao() }
    single { get<AppDatabase>().stickerShareTimeDao() }
}