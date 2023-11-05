package com.skyd.rays.ui.screen

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.skyd.rays.R
import com.skyd.rays.ext.isCompact
import com.skyd.rays.ui.local.LocalCustomPrimaryColor
import com.skyd.rays.ui.local.LocalWindowSizeClass
import com.skyd.rays.ui.screen.home.HomeScreen
import com.skyd.rays.ui.screen.minitool.MiniToolScreen
import com.skyd.rays.ui.screen.more.MoreScreen
import kotlinx.coroutines.launch

const val MAIN_SCREEN_ROUTE = "mainScreen"

@Composable
fun MainScreen() {
    val windowSizeClass = LocalWindowSizeClass.current
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState { 3 }
    val navigationBarOrRail = @Composable {
        NavigationBarOrRail(
            currentPage = pagerState.currentPage,
            scrollToPage = {
                coroutineScope.launch {
                    pagerState.scrollToPage(it)
                }
            }
        )
    }
    val contentPager: @Composable (modifier: Modifier) -> Unit = @Composable { modifier ->
        ContentPager(modifier = modifier, pagerState = pagerState)
    }

    Scaffold(
        bottomBar = {
            if (windowSizeClass.isCompact) {
                navigationBarOrRail()
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { padding ->
        // 重绘一下当前 page，以便刷新主题色
        LaunchedEffect(LocalCustomPrimaryColor.current) {
            pagerState.scrollToPage(pagerState.currentPage)
        }

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
                navigationBarOrRail()
            }
            contentPager(Modifier.weight(1f))
        }
    }
}

@Composable
private fun NavigationBarOrRail(
    currentPage: Int,
    scrollToPage: (Int) -> Unit
) {
    val items = listOf(
        stringResource(R.string.home_screen_name),
        stringResource(R.string.mini_tool_screen_name),
        stringResource(R.string.more_screen_name)
    )
    val icons = remember {
        mapOf(
            true to listOf(Icons.Filled.Home, Icons.Filled.Extension, Icons.Filled.Egg),
            false to listOf(Icons.Outlined.Home, Icons.Outlined.Extension, Icons.Outlined.Egg),
        )
    }
    val windowSizeClass = LocalWindowSizeClass.current

    if (windowSizeClass.isCompact) {
        NavigationBar {
            items.forEachIndexed { index, item ->
                val selected = currentPage == index
                NavigationBarItem(
                    icon = { Icon(icons[selected]!![index], contentDescription = item) },
                    label = { Text(item) },
                    selected = selected,
                    onClick = { scrollToPage(index) }
                )
            }
        }
    } else {
        NavigationRail {
            items.forEachIndexed { index, item ->
                val selected = currentPage == index
                NavigationRailItem(
                    icon = { Icon(icons[selected]!![index], contentDescription = item) },
                    label = { Text(item) },
                    selected = selected,
                    onClick = { scrollToPage(index) }
                )
            }
        }
    }
}

@Composable
private fun ContentPager(modifier: Modifier, pagerState: PagerState) {
    HorizontalPager(
        state = pagerState,
        modifier = modifier,
        userScrollEnabled = false
    ) { page ->
        when (page) {
            0 -> HomeScreen()
            1 -> MiniToolScreen()
            2 -> MoreScreen()
        }
    }
}