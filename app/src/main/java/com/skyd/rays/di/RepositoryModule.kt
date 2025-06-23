package com.skyd.rays.di

import com.skyd.rays.model.respository.AddRepository
import com.skyd.rays.model.respository.ApiGrantRepository
import com.skyd.rays.model.respository.ClassificationModelRepository
import com.skyd.rays.model.respository.DataRepository
import com.skyd.rays.model.respository.DetailRepository
import com.skyd.rays.model.respository.HomeRepository
import com.skyd.rays.model.respository.ImageSearchRepository
import com.skyd.rays.model.respository.ImportExportFilesRepository
import com.skyd.rays.model.respository.MergeStickersRepository
import com.skyd.rays.model.respository.SearchConfigRepository
import com.skyd.rays.model.respository.SearchRepository
import com.skyd.rays.model.respository.SelfieSegmentationRepository
import com.skyd.rays.model.respository.StyleTransferRepository
import com.skyd.rays.model.respository.UpdateRepository
import com.skyd.rays.model.respository.UriStringShareRepository
import com.skyd.rays.model.respository.WebDavRepository
import org.koin.dsl.module

val repositoryModule = module {
    factory { AddRepository(get(), get()) }
    factory { DataRepository(get(), get(), get()) }
    factory { SearchConfigRepository(get()) }
    factory { ImageSearchRepository(get(), get()) }
    factory { UriStringShareRepository(get()) }
    factory { MergeStickersRepository(get(), get(), get()) }
    factory { HomeRepository(get(), get()) }
    factory { SearchRepository(get(), get(), get()) }
    factory { UpdateRepository(get()) }
    factory { ImportExportFilesRepository(get(), get()) }
    factory { ClassificationModelRepository() }
    factory { DetailRepository(get()) }
    factory { WebDavRepository(get(), get()) }
    factory { StyleTransferRepository() }
    factory { ApiGrantRepository(get()) }
    factory { SelfieSegmentationRepository() }
}