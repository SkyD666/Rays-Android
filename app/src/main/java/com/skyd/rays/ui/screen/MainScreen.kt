package com.skyd.rays.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Egg
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Egg
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.skyd.rays.R
import com.skyd.rays.ui.local.LocalWindowSizeClass
import com.skyd.rays.ui.screen.home.HomeScreen
import com.skyd.rays.ui.screen.more.MoreScreen
import kotlinx.coroutines.launch

const val MAIN_SCREEN_ROUTE = "mainScreen"

@Composable
fun MainScreen() {
    val windowSizeClass = LocalWindowSizeClass.current
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState { 2 }
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
    val contentPager = @Composable {
        ContentPager(pagerState = pagerState)
    }

    if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f)) {
                contentPager()
            }
            navigationBarOrRail()
        }
    } else {
        Row(modifier = Modifier.fillMaxSize()) {
            navigationBarOrRail()
            Box(modifier = Modifier.weight(1f)) {
                contentPager()
            }
        }
    }
}

@Composable
private fun NavigationBarOrRail(
    currentPage: Int,
    scrollToPage: (Int) -> Unit
) {
    val items = listOf(
        stringResource(R.string.home_screen_name), stringResource(R.string.more_screen_name)
    )
    val icons = remember {
        mapOf(
            true to listOf(Icons.Filled.Home, Icons.Filled.Egg),
            false to listOf(Icons.Outlined.Home, Icons.Outlined.Egg),
        )
    }
    val windowSizeClass = LocalWindowSizeClass.current

    if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact) {
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
private fun ContentPager(pagerState: PagerState) {
    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize(),
        userScrollEnabled = false
    ) { page ->
        when (page) {
            0 -> {
                HomeScreen()
            }

            1 -> {
                MoreScreen()
            }
        }
    }
}