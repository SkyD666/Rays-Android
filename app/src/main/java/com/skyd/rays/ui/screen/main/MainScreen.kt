package com.skyd.rays.ui.screen.main

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Egg
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Egg
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.skyd.rays.R
import com.skyd.rays.ext.isCompact
import com.skyd.rays.ui.local.LocalWindowSizeClass
import com.skyd.rays.ui.screen.home.HOME_SCREEN_ROUTE
import com.skyd.rays.ui.screen.home.HomeScreen
import com.skyd.rays.ui.screen.minitool.MINI_TOOL_SCREEN_ROUTE
import com.skyd.rays.ui.screen.minitool.MiniToolScreen
import com.skyd.rays.ui.screen.more.MORE_SCREEN_ROUTE
import com.skyd.rays.ui.screen.more.MoreScreen

const val MAIN_SCREEN_ROUTE = "mainScreen"

@Composable
fun MainScreen() {
    val windowSizeClass = LocalWindowSizeClass.current
    val mainNavController = rememberNavController()

    Scaffold(
        bottomBar = {
            if (windowSizeClass.isCompact) {
                NavigationBarOrRail(navController = mainNavController)
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { padding ->
        Row(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding)
                .run {
                    if (!windowSizeClass.isCompact) {
                        windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
                    } else this
                },
        ) {
            if (!windowSizeClass.isCompact) {
                NavigationBarOrRail(navController = mainNavController)
            }
            NavHost(
                navController = mainNavController,
                startDestination = HOME_SCREEN_ROUTE,
                modifier = Modifier.weight(1f),
                enterTransition = { fadeIn(animationSpec = tween(200)) },
                exitTransition = { fadeOut(animationSpec = tween(200)) },
                popEnterTransition = { fadeIn(animationSpec = tween(200)) },
                popExitTransition = { fadeOut(animationSpec = tween(200)) },
            ) {
                composable(HOME_SCREEN_ROUTE) { HomeScreen() }
                composable(MINI_TOOL_SCREEN_ROUTE) { MiniToolScreen() }
                composable(MORE_SCREEN_ROUTE) { MoreScreen() }
            }
        }
    }
}

@Composable
private fun NavigationBarOrRail(navController: NavController) {
    var currentPage by rememberSaveable { mutableIntStateOf(0) }
    val items = listOf(
        stringResource(R.string.home_screen_name) to HOME_SCREEN_ROUTE,
        stringResource(R.string.mini_tool_screen_name) to MINI_TOOL_SCREEN_ROUTE,
        stringResource(R.string.more_screen_name) to MORE_SCREEN_ROUTE
    )
    val icons = remember {
        mapOf(
            true to listOf(Icons.Filled.Home, Icons.Filled.Extension, Icons.Filled.Egg),
            false to listOf(Icons.Outlined.Home, Icons.Outlined.Extension, Icons.Outlined.Egg),
        )
    }
    val windowSizeClass = LocalWindowSizeClass.current

    val onClick: (Int) -> Unit = { index ->
        navController.navigate(items[index].second) {
            // Pop up to the start destination of the graph to
            // avoid building up a large stack of destinations
            // on the back stack as users select items
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            // Avoid multiple copies of the same destination when
            // reselecting the same item
            launchSingleTop = true
            // Restore state when reselecting a previously selected item
            restoreState = true
        }
        currentPage = index
    }

    if (windowSizeClass.isCompact) {
        NavigationBar {
            items.forEachIndexed { index, item ->
                val selected = currentPage == index
                NavigationBarItem(
                    icon = { Icon(icons[selected]!![index], contentDescription = item.first) },
                    label = { Text(item.first) },
                    selected = selected,
                    onClick = { onClick(index) }
                )
            }
        }
    } else {
        NavigationRail {
            items.forEachIndexed { index, item ->
                val selected = currentPage == index
                NavigationRailItem(
                    icon = { Icon(icons[selected]!![index], contentDescription = item.first) },
                    label = { Text(item.first) },
                    selected = selected,
                    onClick = { onClick(index) }
                )
            }
        }
    }
}
