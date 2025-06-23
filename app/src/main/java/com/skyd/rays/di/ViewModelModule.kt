package com.skyd.rays.di

import com.skyd.rays.ui.activity.MainViewModel
import com.skyd.rays.ui.screen.about.update.UpdateViewModel
import com.skyd.rays.ui.screen.add.AddViewModel
import com.skyd.rays.ui.screen.detail.DetailViewModel
import com.skyd.rays.ui.screen.home.HomeViewModel
import com.skyd.rays.ui.screen.mergestickers.MergeStickersViewModel
import com.skyd.rays.ui.screen.minitool.selfiesegmentation.SelfieSegmentationViewModel
import com.skyd.rays.ui.screen.minitool.styletransfer.StyleTransferViewModel
import com.skyd.rays.ui.screen.search.SearchViewModel
import com.skyd.rays.ui.screen.search.imagesearch.ImageSearchViewModel
import com.skyd.rays.ui.screen.search.multiselect.MultiSelectViewModel
import com.skyd.rays.ui.screen.settings.api.apigrant.ApiGrantViewModel
import com.skyd.rays.ui.screen.settings.data.DataViewModel
import com.skyd.rays.ui.screen.settings.data.cache.CacheViewModel
import com.skyd.rays.ui.screen.settings.data.importexport.cloud.webdav.WebDavViewModel
import com.skyd.rays.ui.screen.settings.data.importexport.file.exportfiles.ExportFilesViewModel
import com.skyd.rays.ui.screen.settings.data.importexport.file.importfiles.ImportFilesViewModel
import com.skyd.rays.ui.screen.settings.ml.classification.model.ClassificationModelViewModel
import com.skyd.rays.ui.screen.settings.searchconfig.SearchConfigViewModel
import com.skyd.rays.ui.screen.settings.shareconfig.uristringshare.UriStringShareViewModel
import com.skyd.rays.ui.screen.stickerslist.StickersListViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { UpdateViewModel(get()) }
    viewModel { HomeViewModel(get()) }
    viewModel { AddViewModel(get(), get()) }
    viewModel { DetailViewModel(get()) }
    viewModel { SearchViewModel(get()) }
    viewModel { ImageSearchViewModel(get()) }
    viewModel { SelfieSegmentationViewModel(get()) }
    viewModel { ApiGrantViewModel(get()) }
    viewModel { StyleTransferViewModel(get()) }
    viewModel { MultiSelectViewModel(get()) }
    viewModel { CacheViewModel(get()) }
    viewModel { ExportFilesViewModel(get()) }
    viewModel { DataViewModel(get()) }
    viewModel { ClassificationModelViewModel(get()) }
    viewModel { WebDavViewModel(get()) }
    viewModel { StickersListViewModel(get()) }
    viewModel { MergeStickersViewModel(get()) }
    viewModel { SearchConfigViewModel(get(), get()) }
    viewModel { MainViewModel() }
    viewModel { UriStringShareViewModel(get()) }
    viewModel { ImportFilesViewModel(get()) }
}