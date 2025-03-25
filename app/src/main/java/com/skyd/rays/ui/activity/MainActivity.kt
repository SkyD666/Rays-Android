package com.skyd.rays.ui.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.IntentCompat
import androidx.core.os.BundleCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.util.Consumer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import com.skyd.rays.base.BaseComposeActivity
import com.skyd.rays.ext.dataStore
import com.skyd.rays.ext.getOrDefault
import com.skyd.rays.ext.listType
import com.skyd.rays.ext.serializableType
import com.skyd.rays.ext.startWith
import com.skyd.rays.model.bean.UriWithStickerUuidBean
import com.skyd.rays.model.preference.privacy.DisableScreenshotPreference
import com.skyd.rays.ui.local.LocalCurrentStickerUuid
import com.skyd.rays.ui.local.LocalNavController
import com.skyd.rays.ui.screen.about.AboutRoute
import com.skyd.rays.ui.screen.about.AboutScreen
import com.skyd.rays.ui.screen.about.license.LicenseRoute
import com.skyd.rays.ui.screen.about.license.LicenseScreen
import com.skyd.rays.ui.screen.about.update.UpdateDialog
import com.skyd.rays.ui.screen.add.AddDeepLinkRoute
import com.skyd.rays.ui.screen.add.AddRoute
import com.skyd.rays.ui.screen.add.AddScreen
import com.skyd.rays.ui.screen.detail.DetailRoute
import com.skyd.rays.ui.screen.detail.DetailScreen
import com.skyd.rays.ui.screen.fullimage.FullImageRoute
import com.skyd.rays.ui.screen.fullimage.FullImageScreen
import com.skyd.rays.ui.screen.fullimage.WrappedUri
import com.skyd.rays.ui.screen.fullimage.WrappedUriNullable
import com.skyd.rays.ui.screen.main.MainRoute
import com.skyd.rays.ui.screen.main.MainScreen
import com.skyd.rays.ui.screen.mergestickers.MergeStickersRoute
import com.skyd.rays.ui.screen.mergestickers.MergeStickersScreenRoute
import com.skyd.rays.ui.screen.minitool.selfiesegmentation.SelfieSegmentationRoute
import com.skyd.rays.ui.screen.minitool.selfiesegmentation.SelfieSegmentationScreen
import com.skyd.rays.ui.screen.minitool.styletransfer.StyleTransferRoute
import com.skyd.rays.ui.screen.minitool.styletransfer.StyleTransferScreen
import com.skyd.rays.ui.screen.search.SearchRoute
import com.skyd.rays.ui.screen.search.SearchScreen
import com.skyd.rays.ui.screen.search.imagesearch.ImageSearchRoute
import com.skyd.rays.ui.screen.search.imagesearch.ImageSearchScreen
import com.skyd.rays.ui.screen.settings.SettingsRoute
import com.skyd.rays.ui.screen.settings.SettingsScreen
import com.skyd.rays.ui.screen.settings.api.ApiRoute
import com.skyd.rays.ui.screen.settings.api.ApiScreen
import com.skyd.rays.ui.screen.settings.api.apigrant.ApiGrantRoute
import com.skyd.rays.ui.screen.settings.api.apigrant.ApiGrantScreen
import com.skyd.rays.ui.screen.settings.appearance.AppearanceRoute
import com.skyd.rays.ui.screen.settings.appearance.AppearanceScreen
import com.skyd.rays.ui.screen.settings.appearance.style.SearchStyleRoute
import com.skyd.rays.ui.screen.settings.appearance.style.SearchStyleScreen
import com.skyd.rays.ui.screen.settings.data.DataRoute
import com.skyd.rays.ui.screen.settings.data.DataScreen
import com.skyd.rays.ui.screen.settings.data.cache.CacheRoute
import com.skyd.rays.ui.screen.settings.data.cache.CacheScreen
import com.skyd.rays.ui.screen.settings.data.importexport.ImportExportRoute
import com.skyd.rays.ui.screen.settings.data.importexport.ImportExportScreen
import com.skyd.rays.ui.screen.settings.data.importexport.cloud.webdav.WebDavRoute
import com.skyd.rays.ui.screen.settings.data.importexport.cloud.webdav.WebDavScreen
import com.skyd.rays.ui.screen.settings.data.importexport.file.exportfiles.ExportFilesRoute
import com.skyd.rays.ui.screen.settings.data.importexport.file.exportfiles.ExportFilesScreen
import com.skyd.rays.ui.screen.settings.data.importexport.file.importfiles.ImportFilesRoute
import com.skyd.rays.ui.screen.settings.data.importexport.file.importfiles.ImportFilesScreen
import com.skyd.rays.ui.screen.settings.imagesource.ImageSourceRoute
import com.skyd.rays.ui.screen.settings.imagesource.ImageSourceScreen
import com.skyd.rays.ui.screen.settings.ml.MlRoute
import com.skyd.rays.ui.screen.settings.ml.MlScreen
import com.skyd.rays.ui.screen.settings.ml.classification.ClassificationRoute
import com.skyd.rays.ui.screen.settings.ml.classification.ClassificationScreen
import com.skyd.rays.ui.screen.settings.ml.classification.model.ClassificationModelRoute
import com.skyd.rays.ui.screen.settings.ml.classification.model.ClassificationModelScreen
import com.skyd.rays.ui.screen.settings.ml.textrecognize.TextRecognizeRoute
import com.skyd.rays.ui.screen.settings.ml.textrecognize.TextRecognizeScreen
import com.skyd.rays.ui.screen.settings.privacy.PrivacyRoute
import com.skyd.rays.ui.screen.settings.privacy.PrivacyScreen
import com.skyd.rays.ui.screen.settings.privacy.blurstickers.BlurStickersRoute
import com.skyd.rays.ui.screen.settings.privacy.blurstickers.BlurStickersScreen
import com.skyd.rays.ui.screen.settings.searchconfig.SearchConfigRoute
import com.skyd.rays.ui.screen.settings.searchconfig.SearchConfigScreen
import com.skyd.rays.ui.screen.settings.shareconfig.ShareConfigRoute
import com.skyd.rays.ui.screen.settings.shareconfig.ShareConfigScreen
import com.skyd.rays.ui.screen.settings.shareconfig.autoshare.AutoShareRoute
import com.skyd.rays.ui.screen.settings.shareconfig.autoshare.AutoShareScreen
import com.skyd.rays.ui.screen.settings.shareconfig.uristringshare.UriStringShareRoute
import com.skyd.rays.ui.screen.settings.shareconfig.uristringshare.UriStringShareScreen
import com.skyd.rays.ui.screen.stickerslist.StickersListRoute
import com.skyd.rays.ui.screen.stickerslist.StickersListScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.reflect.typeOf


@AndroidEntryPoint
class MainActivity : BaseComposeActivity() {
    private lateinit var navController: NavHostController
    private val viewModel: MainViewModel by viewModels()
    private val intentChannel = Channel<MainIntent>(Channel.UNLIMITED)
    private val dispatch = { intent: MainIntent ->
        intentChannel.trySend(intent).getOrThrow()
    }
    private var needHandleIntent = true

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        // 是否禁止截图
        if (dataStore.getOrDefault(DisableScreenshotPreference)) {
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }

        lifecycleScope.launch(Dispatchers.Main.immediate) {
            intentChannel
                .consumeAsFlow()
                .startWith(MainIntent.Init)
                .onEach(viewModel::processIntent)
                .collect()
        }

        setContentBase {
            navController = rememberNavController()

            // 更新主题色
            val stickerUuid = LocalCurrentStickerUuid.current
            LaunchedEffect(stickerUuid) {
                dispatch(MainIntent.UpdateThemeColor(stickerUuid))
            }

            if (needHandleIntent) {
                LaunchedEffect(Unit) {
                    needHandleIntent = false
                    navController.handleDeepLink(intent)
                }
            }
            DisposableEffect(navController) {
                val listener = Consumer<Intent> { newIntent ->
                    navController.handleDeepLink(newIntent)
                }
                addOnNewIntentListener(listener)
                onDispose { removeOnNewIntentListener(listener) }
            }

            CompositionLocalProvider(LocalNavController provides navController) {
                AppContent()
            }
        }
    }

    @Composable
    private fun AppContent() {
        var openUpdateDialog by rememberSaveable { mutableStateOf(true) }

        NavHost(
            modifier = Modifier.background(MaterialTheme.colorScheme.background),
            navController = navController,
            startDestination = MainRoute,
            enterTransition = {
                fadeIn(animationSpec = tween(220, delayMillis = 30)) + scaleIn(
                    animationSpec = tween(220, delayMillis = 30),
                    initialScale = 0.92f,
                )
            },
            exitTransition = { fadeOut(animationSpec = tween(90)) },
            popEnterTransition = {
                fadeIn(animationSpec = tween(220)) + scaleIn(
                    animationSpec = tween(220),
                    initialScale = 0.92f,
                )
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(220)) + scaleOut(
                    animationSpec = tween(220),
                    targetScale = 0.92f,
                )
            },
        ) {
            composable<MainRoute> { MainScreen() }
            composable<AddRoute>(
                typeMap = mapOf(typeOf<List<UriWithStickerUuidBean>>() to listType<UriWithStickerUuidBean>())
            ) {
                val route = it.toRoute<AddRoute>()
                AddScreen(
                    initStickers = route.stickers.toMutableList(),
                    isEdit = route.isEdit,
                )
            }
            composable<AddDeepLinkRoute>(
                deepLinks = listOf(Intent.ACTION_SEND, Intent.ACTION_SEND_MULTIPLE).map {
                    navDeepLink {
                        action = it
                        mimeType = "image/*"
                    }
                },
            ) {
                AddScreen(
                    initStickers = it.arguments?.let { arguments ->
                        initIntent(
                            BundleCompat.getParcelable(
                                arguments,
                                NavController.KEY_DEEP_LINK_INTENT,
                                Intent::class.java,
                            )
                        )
                    } ?: mutableListOf(),
                    isEdit = false,
                )
            }
            composable<SettingsRoute> { SettingsScreen() }
            composable<MlRoute> { MlScreen() }
            composable<ClassificationRoute> { ClassificationScreen() }
            composable<ClassificationModelRoute> { ClassificationModelScreen() }
            composable<TextRecognizeRoute> { TextRecognizeScreen() }
            composable<SearchConfigRoute> { SearchConfigScreen() }
            composable<AppearanceRoute> { AppearanceScreen() }
            composable<SearchStyleRoute> { SearchStyleScreen() }
            composable<AboutRoute> { AboutScreen() }
            composable<LicenseRoute> { LicenseScreen() }
            composable<ImportExportRoute> { ImportExportScreen() }
            composable<WebDavRoute> { WebDavScreen() }
            composable<ExportFilesRoute> {
                ExportFilesScreen(exportStickers = it.toRoute<ExportFilesRoute>().exportStickers)
            }
            composable<MergeStickersRoute> {
                MergeStickersScreenRoute(stickerUuids = it.toRoute<MergeStickersRoute>().stickerUuids)
            }
            composable<ImportFilesRoute> { ImportFilesScreen() }
            composable<DataRoute> { DataScreen() }
            composable<ShareConfigRoute> { ShareConfigScreen() }
            composable<UriStringShareRoute> { UriStringShareScreen() }
            composable<StyleTransferRoute> { StyleTransferScreen() }
            composable<SelfieSegmentationRoute> { SelfieSegmentationScreen() }
            composable<ApiRoute> { ApiScreen() }
            composable<ApiGrantRoute> { ApiGrantScreen() }
            composable<AutoShareRoute> { AutoShareScreen() }
            composable<PrivacyRoute> { PrivacyScreen() }
            composable<DetailRoute> { DetailScreen(stickerUuid = it.toRoute<DetailRoute>().stickerUuid) }
            composable<FullImageRoute>(
                typeMap = mapOf(typeOf<WrappedUri>() to serializableType<WrappedUri>())
            ) {
                FullImageScreen(image = it.toRoute<FullImageRoute>().uri.image)
            }
            composable<StickersListRoute> { StickersListScreen(query = it.toRoute<StickersListRoute>().query) }
            composable<SearchRoute> { SearchScreen() }
            composable<ImageSearchRoute>(
                typeMap = mapOf(typeOf<WrappedUriNullable>() to serializableType<WrappedUriNullable>())
            ) {
                ImageSearchScreen(baseImage = it.toRoute<ImageSearchRoute>().uri.image)
            }
            composable<ImageSourceRoute> { ImageSourceScreen() }
            composable<BlurStickersRoute> { BlurStickersScreen() }
            composable<CacheRoute> { CacheScreen() }
        }

        if (openUpdateDialog) {
            UpdateDialog(
                silence = true,
                onClosed = { openUpdateDialog = false },
                onError = { openUpdateDialog = false },
            )
        }
    }

    private fun initIntent(intent: Intent?): MutableList<UriWithStickerUuidBean> {
        val stickers: MutableList<UriWithStickerUuidBean> = when (intent?.action) {
            Intent.ACTION_SEND -> {
                val data = mutableListOf<UriWithStickerUuidBean>()
                if (intent.type?.startsWith("image/") == true) {
                    val uri = IntentCompat.getParcelableExtra(
                        intent, Intent.EXTRA_STREAM, Uri::class.java,
                    )
                    if (uri != null) {
                        data.add(UriWithStickerUuidBean(uri = uri))
                    }
                    data
                } else data
            }

            Intent.ACTION_SEND_MULTIPLE -> {
                val data = mutableListOf<UriWithStickerUuidBean>()
                if (intent.type?.startsWith("image/") == true) {
                    val uris = IntentCompat.getParcelableArrayListExtra(
                        intent, Intent.EXTRA_STREAM, Uri::class.java,
                    )?.map { UriWithStickerUuidBean(uri = it) }
                    if (uris != null) {
                        data.addAll(uris)
                    }
                    data
                } else data
            }

            else -> mutableListOf()
        }

        return stickers
    }
}
