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
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.skyd.rays.base.BaseComposeActivity
import com.skyd.rays.ext.dataStore
import com.skyd.rays.ext.getOrDefault
import com.skyd.rays.ext.startWith
import com.skyd.rays.model.bean.UriWithStickerUuidBean
import com.skyd.rays.model.preference.privacy.DisableScreenshotPreference
import com.skyd.rays.ui.local.LocalCurrentStickerUuid
import com.skyd.rays.ui.local.LocalNavController
import com.skyd.rays.ui.screen.about.ABOUT_SCREEN_ROUTE
import com.skyd.rays.ui.screen.about.AboutScreen
import com.skyd.rays.ui.screen.about.license.LICENSE_SCREEN_ROUTE
import com.skyd.rays.ui.screen.about.license.LicenseScreen
import com.skyd.rays.ui.screen.about.update.UpdateDialog
import com.skyd.rays.ui.screen.add.ADD_SCREEN_ROUTE
import com.skyd.rays.ui.screen.add.AddScreen
import com.skyd.rays.ui.screen.detail.DETAIL_SCREEN_ROUTE
import com.skyd.rays.ui.screen.detail.DetailScreen
import com.skyd.rays.ui.screen.fullimage.FULL_IMAGE_SCREEN_IMAGE_KEY
import com.skyd.rays.ui.screen.fullimage.FULL_IMAGE_SCREEN_ROUTE
import com.skyd.rays.ui.screen.fullimage.FullImageScreen
import com.skyd.rays.ui.screen.main.MAIN_SCREEN_ROUTE
import com.skyd.rays.ui.screen.main.MainScreen
import com.skyd.rays.ui.screen.mergestickers.MERGE_STICKERS_SCREEN_ROUTE
import com.skyd.rays.ui.screen.mergestickers.MERGE_STICKERS_SCREEN_STICKER_UUIDS_KEY
import com.skyd.rays.ui.screen.mergestickers.MergeStickersScreenRoute
import com.skyd.rays.ui.screen.minitool.selfiesegmentation.SELFIE_SEGMENTATION_SCREEN_ROUTE
import com.skyd.rays.ui.screen.minitool.selfiesegmentation.SelfieSegmentationScreen
import com.skyd.rays.ui.screen.minitool.styletransfer.STYLE_TRANSFER_SCREEN_ROUTE
import com.skyd.rays.ui.screen.minitool.styletransfer.StyleTransferScreen
import com.skyd.rays.ui.screen.search.SEARCH_SCREEN_ROUTE
import com.skyd.rays.ui.screen.search.SearchScreen
import com.skyd.rays.ui.screen.search.imagesearch.BASE_IMAGE_KEY
import com.skyd.rays.ui.screen.search.imagesearch.IMAGE_SEARCH_SCREEN_ROUTE
import com.skyd.rays.ui.screen.search.imagesearch.ImageSearchScreen
import com.skyd.rays.ui.screen.settings.SETTINGS_SCREEN_ROUTE
import com.skyd.rays.ui.screen.settings.SettingsScreen
import com.skyd.rays.ui.screen.settings.api.API_SCREEN_ROUTE
import com.skyd.rays.ui.screen.settings.api.ApiScreen
import com.skyd.rays.ui.screen.settings.api.apigrant.API_GRANT_SCREEN_ROUTE
import com.skyd.rays.ui.screen.settings.api.apigrant.ApiGrantScreen
import com.skyd.rays.ui.screen.settings.appearance.APPEARANCE_SCREEN_ROUTE
import com.skyd.rays.ui.screen.settings.appearance.AppearanceScreen
import com.skyd.rays.ui.screen.settings.appearance.style.SEARCH_STYLE_SCREEN_ROUTE
import com.skyd.rays.ui.screen.settings.appearance.style.SearchStyleScreen
import com.skyd.rays.ui.screen.settings.data.DATA_SCREEN_ROUTE
import com.skyd.rays.ui.screen.settings.data.DataScreen
import com.skyd.rays.ui.screen.settings.data.cache.CACHE_SCREEN_ROUTE
import com.skyd.rays.ui.screen.settings.data.cache.CacheScreen
import com.skyd.rays.ui.screen.settings.data.importexport.IMPORT_EXPORT_SCREEN_ROUTE
import com.skyd.rays.ui.screen.settings.data.importexport.ImportExportScreen
import com.skyd.rays.ui.screen.settings.data.importexport.cloud.webdav.WEBDAV_SCREEN_ROUTE
import com.skyd.rays.ui.screen.settings.data.importexport.cloud.webdav.WebDavScreen
import com.skyd.rays.ui.screen.settings.data.importexport.file.exportfiles.EXPORT_FILES_SCREEN_ROUTE
import com.skyd.rays.ui.screen.settings.data.importexport.file.exportfiles.ExportFilesScreen
import com.skyd.rays.ui.screen.settings.data.importexport.file.importfiles.IMPORT_FILES_SCREEN_ROUTE
import com.skyd.rays.ui.screen.settings.data.importexport.file.importfiles.ImportFilesScreen
import com.skyd.rays.ui.screen.settings.imagesource.IMAGE_SOURCE_SCREEN_ROUTE
import com.skyd.rays.ui.screen.settings.imagesource.ImageSourceScreen
import com.skyd.rays.ui.screen.settings.ml.ML_SCREEN_ROUTE
import com.skyd.rays.ui.screen.settings.ml.MlScreen
import com.skyd.rays.ui.screen.settings.ml.classification.CLASSIFICATION_SCREEN_ROUTE
import com.skyd.rays.ui.screen.settings.ml.classification.ClassificationScreen
import com.skyd.rays.ui.screen.settings.ml.classification.model.CLASSIFICATION_MODEL_SCREEN_ROUTE
import com.skyd.rays.ui.screen.settings.ml.classification.model.ClassificationModelScreen
import com.skyd.rays.ui.screen.settings.ml.textrecognize.TEXT_RECOGNIZE_SCREEN_ROUTE
import com.skyd.rays.ui.screen.settings.ml.textrecognize.TextRecognizeScreen
import com.skyd.rays.ui.screen.settings.privacy.PRIVACY_SCREEN_ROUTE
import com.skyd.rays.ui.screen.settings.privacy.PrivacyScreen
import com.skyd.rays.ui.screen.settings.privacy.blurstickers.BLUR_STICKERS_SCREEN_ROUTE
import com.skyd.rays.ui.screen.settings.privacy.blurstickers.BlurStickersScreen
import com.skyd.rays.ui.screen.settings.searchconfig.SEARCH_CONFIG_SCREEN_ROUTE
import com.skyd.rays.ui.screen.settings.searchconfig.SearchConfigScreen
import com.skyd.rays.ui.screen.settings.shareconfig.SHARE_CONFIG_SCREEN_ROUTE
import com.skyd.rays.ui.screen.settings.shareconfig.ShareConfigScreen
import com.skyd.rays.ui.screen.settings.shareconfig.autoshare.AUTO_SHARE_SCREEN_ROUTE
import com.skyd.rays.ui.screen.settings.shareconfig.autoshare.AutoShareScreen
import com.skyd.rays.ui.screen.settings.shareconfig.uristringshare.URI_STRING_SHARE_SCREEN_ROUTE
import com.skyd.rays.ui.screen.settings.shareconfig.uristringshare.UriStringShareScreen
import com.skyd.rays.ui.screen.stickerslist.STICKERS_LIST_SCREEN_ROUTE
import com.skyd.rays.ui.screen.stickerslist.StickersListScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch


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
            startDestination = MAIN_SCREEN_ROUTE,
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
            composable(route = MAIN_SCREEN_ROUTE) {
                MainScreen()
            }
            composable(
                route = "$ADD_SCREEN_ROUTE?isEdit={isEdit}",
                arguments = listOf(navArgument("isEdit") { defaultValue = false }),
                deepLinks = listOf(
                    navDeepLink {
                        action = Intent.ACTION_SEND
                        mimeType = "image/*"
                    },
                    navDeepLink {
                        action = Intent.ACTION_SEND_MULTIPLE
                        mimeType = "image/*"
                    },
                )
            ) {
                val arguments = it.arguments
                val externalUris: MutableList<UriWithStickerUuidBean> = if (arguments != null) {
                    // stickers from external
                    initIntent(
                        BundleCompat.getParcelable(
                            arguments,
                            NavController.KEY_DEEP_LINK_INTENT,
                            Intent::class.java,
                        )
                    )
                } else mutableListOf()

                if (externalUris.isNotEmpty()) {
                    // stickers from external
                    AddScreen(
                        initStickers = externalUris,
                        isEdit = false,
                    )
                } else {
                    // stickers from self
                    AddScreen(
                        initStickers = arguments?.let { bundle ->
                            BundleCompat.getParcelableArrayList(
                                bundle, "stickers", UriWithStickerUuidBean::class.java,
                            )
                        } ?: mutableListOf(),
                        isEdit = it.arguments?.getBoolean("isEdit") == true,
                    )
                }
            }
            composable(route = SETTINGS_SCREEN_ROUTE) {
                SettingsScreen()
            }
            composable(route = ML_SCREEN_ROUTE) {
                MlScreen()
            }
            composable(route = CLASSIFICATION_SCREEN_ROUTE) {
                ClassificationScreen()
            }
            composable(route = CLASSIFICATION_MODEL_SCREEN_ROUTE) {
                ClassificationModelScreen()
            }
            composable(route = TEXT_RECOGNIZE_SCREEN_ROUTE) {
                TextRecognizeScreen()
            }
            composable(route = SEARCH_CONFIG_SCREEN_ROUTE) {
                SearchConfigScreen()
            }
            composable(route = APPEARANCE_SCREEN_ROUTE) {
                AppearanceScreen()
            }
            composable(route = SEARCH_STYLE_SCREEN_ROUTE) {
                SearchStyleScreen()
            }
            composable(route = ABOUT_SCREEN_ROUTE) {
                AboutScreen()
            }
            composable(route = LICENSE_SCREEN_ROUTE) {
                LicenseScreen()
            }
            composable(route = IMPORT_EXPORT_SCREEN_ROUTE) {
                ImportExportScreen()
            }
            composable(route = WEBDAV_SCREEN_ROUTE) {
                WebDavScreen()
            }
            composable(route = EXPORT_FILES_SCREEN_ROUTE) {
                ExportFilesScreen(
                    exportStickers = it.arguments?.getStringArrayList("exportStickers")
                )
            }
            composable(route = MERGE_STICKERS_SCREEN_ROUTE) {
                MergeStickersScreenRoute(
                    stickerUuids = it.arguments?.getStringArrayList(
                        MERGE_STICKERS_SCREEN_STICKER_UUIDS_KEY
                    )
                )
            }
            composable(route = IMPORT_FILES_SCREEN_ROUTE) {
                ImportFilesScreen()
            }
            composable(route = DATA_SCREEN_ROUTE) {
                DataScreen()
            }
            composable(route = SHARE_CONFIG_SCREEN_ROUTE) {
                ShareConfigScreen()
            }
            composable(route = URI_STRING_SHARE_SCREEN_ROUTE) {
                UriStringShareScreen()
            }
            composable(route = STYLE_TRANSFER_SCREEN_ROUTE) {
                StyleTransferScreen()
            }
            composable(route = SELFIE_SEGMENTATION_SCREEN_ROUTE) {
                SelfieSegmentationScreen()
            }
            composable(route = API_SCREEN_ROUTE) {
                ApiScreen()
            }
            composable(route = API_GRANT_SCREEN_ROUTE) {
                ApiGrantScreen()
            }
            composable(route = AUTO_SHARE_SCREEN_ROUTE) {
                AutoShareScreen()
            }
            composable(route = PRIVACY_SCREEN_ROUTE) {
                PrivacyScreen()
            }
            composable(route = "$DETAIL_SCREEN_ROUTE?stickerUuid={stickerUuid}") {
                DetailScreen(stickerUuid = it.arguments?.getString("stickerUuid").orEmpty())
            }
            composable(route = "$FULL_IMAGE_SCREEN_ROUTE?$FULL_IMAGE_SCREEN_IMAGE_KEY={$FULL_IMAGE_SCREEN_IMAGE_KEY}") {
                FullImageScreen(image = it.arguments?.getParcelable(FULL_IMAGE_SCREEN_IMAGE_KEY)!!)
            }
            composable(route = "$STICKERS_LIST_SCREEN_ROUTE?query={query}") {
                StickersListScreen(query = it.arguments?.getString("query").orEmpty())
            }
            composable(route = SEARCH_SCREEN_ROUTE) {
                SearchScreen()
            }
            composable(route = IMAGE_SEARCH_SCREEN_ROUTE) {
                ImageSearchScreen(
                    baseImage = it.arguments?.getParcelable(BASE_IMAGE_KEY)
                )
            }
            composable(route = IMAGE_SOURCE_SCREEN_ROUTE) {
                ImageSourceScreen()
            }
            composable(route = BLUR_STICKERS_SCREEN_ROUTE) {
                BlurStickersScreen()
            }
            composable(route = CACHE_SCREEN_ROUTE) {
                CacheScreen()
            }
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
