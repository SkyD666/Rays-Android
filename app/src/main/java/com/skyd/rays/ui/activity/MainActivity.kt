package com.skyd.rays.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavHostController
import androidx.navigation.navArgument
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.skyd.rays.ext.navigate
import com.skyd.rays.model.preference.SettingsProvider
import com.skyd.rays.ui.local.LocalDarkMode
import com.skyd.rays.ui.local.LocalNavController
import com.skyd.rays.ui.screen.MAIN_SCREEN_ROUTE
import com.skyd.rays.ui.screen.MainScreen
import com.skyd.rays.ui.screen.about.ABOUT_SCREEN_ROUTE
import com.skyd.rays.ui.screen.about.AboutScreen
import com.skyd.rays.ui.screen.about.license.LICENSE_SCREEN_ROUTE
import com.skyd.rays.ui.screen.about.license.LicenseScreen
import com.skyd.rays.ui.screen.add.ADD_SCREEN_ROUTE
import com.skyd.rays.ui.screen.add.AddScreen
import com.skyd.rays.ui.screen.minitool.MINI_TOOL_SCREEN_ROUTE
import com.skyd.rays.ui.screen.minitool.MiniToolScreen
import com.skyd.rays.ui.screen.settings.SETTINGS_SCREEN_ROUTE
import com.skyd.rays.ui.screen.settings.SettingsScreen
import com.skyd.rays.ui.screen.settings.appearance.APPEARANCE_SCREEN_ROUTE
import com.skyd.rays.ui.screen.settings.appearance.AppearanceScreen
import com.skyd.rays.ui.screen.settings.data.DATA_SCREEN_ROUTE
import com.skyd.rays.ui.screen.settings.data.DataScreen
import com.skyd.rays.ui.screen.settings.data.importexport.IMPORT_EXPORT_SCREEN_ROUTE
import com.skyd.rays.ui.screen.settings.data.importexport.ImportExportScreen
import com.skyd.rays.ui.screen.settings.data.importexport.cloud.webdav.WEBDAV_SCREEN_ROUTE
import com.skyd.rays.ui.screen.settings.data.importexport.cloud.webdav.WebDavScreen
import com.skyd.rays.ui.screen.settings.easyusage.EASY_USAGE_SCREEN_ROUTE
import com.skyd.rays.ui.screen.settings.easyusage.EasyUsageScreen
import com.skyd.rays.ui.screen.settings.searchconfig.SEARCH_CONFIG_SCREEN_ROUTE
import com.skyd.rays.ui.screen.settings.searchconfig.SearchConfigScreen
import com.skyd.rays.ui.theme.RaysTheme
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavHostController

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setContent {
            navController = rememberAnimatedNavController()
            SettingsProvider {
                CompositionLocalProvider(LocalNavController provides navController) {
                    AppContent()
                    initIntent()
                }
            }
        }
    }

    @Composable
    private fun AppContent() {
        RaysTheme(darkTheme = LocalDarkMode.current) {
            AnimatedNavHost(
                modifier = Modifier.background(MaterialTheme.colorScheme.background),
                navController = navController,
                startDestination = MAIN_SCREEN_ROUTE,
                enterTransition = {
                    fadeIn(
                        animationSpec = spring(stiffness = Spring.StiffnessMedium)
                    ) + scaleIn(
                        animationSpec = spring(stiffness = Spring.StiffnessMedium),
                        initialScale = 0.96f
                    )
                },
                exitTransition = {
                    fadeOut(
                        animationSpec = spring(stiffness = Spring.StiffnessMedium)
                    ) + scaleOut(
                        animationSpec = spring(stiffness = Spring.StiffnessMedium),
                        targetScale = 0.96f
                    )
                },
                popEnterTransition = {
                    fadeIn(
                        animationSpec = spring(stiffness = Spring.StiffnessMedium)
                    ) + scaleIn(
                        animationSpec = spring(stiffness = Spring.StiffnessMedium),
                        initialScale = 0.96f
                    )
                },
                popExitTransition = {
                    fadeOut(
                        animationSpec = spring(stiffness = Spring.StiffnessMedium)
                    ) + scaleOut(
                        animationSpec = spring(stiffness = Spring.StiffnessMedium),
                        targetScale = 0.96f
                    )
                },
            ) {
                composable(route = MAIN_SCREEN_ROUTE) {
                    MainScreen()
                }
                composable(
                    route = "$ADD_SCREEN_ROUTE?stickerUuid={stickerUuid}",
                    arguments = listOf(navArgument("stickerUuid") { defaultValue = "" })
                ) {
                    AddScreen(
                        initStickerUuid = it.arguments?.getString("stickerUuid").orEmpty(),
                        sticker = null//it.arguments?.getString("sticker").orEmpty()
                    )
                }
                composable(route = SETTINGS_SCREEN_ROUTE) {
                    SettingsScreen()
                }
                composable(route = SEARCH_CONFIG_SCREEN_ROUTE) {
                    SearchConfigScreen()
                }
                composable(route = APPEARANCE_SCREEN_ROUTE) {
                    AppearanceScreen()
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
                composable(route = EASY_USAGE_SCREEN_ROUTE) {
                    EasyUsageScreen()
                }
                composable(route = WEBDAV_SCREEN_ROUTE) {
                    WebDavScreen()
                }
                composable(route = DATA_SCREEN_ROUTE) {
                    DataScreen()
                }
                composable(route = MINI_TOOL_SCREEN_ROUTE) {
                    MiniToolScreen()
                }
            }
        }
    }

    private fun initIntent() {
        val text = intent.getStringExtra(Intent.EXTRA_PROCESS_TEXT) ?: return
        navController.navigate(ADD_SCREEN_ROUTE, Bundle().apply { putString("sticker", text) })
    }
}
