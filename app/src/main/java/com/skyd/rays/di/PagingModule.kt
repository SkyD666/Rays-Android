package com.skyd.rays.di

import androidx.paging.PagingConfig
import org.koin.dsl.module

val pagingModule = module {
    // enablePlaceholders must be true
    // https://issuetracker.google.com/issues/214253526
    single { PagingConfig(pageSize = 20, enablePlaceholders = true) }
}