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
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.skyd.rays.R
import com.skyd.rays.ext.screenIsLand
import com.skyd.rays.ui.screen.home.HomeScreen
import com.skyd.rays.ui.screen.more.MoreScreen
import kotlinx.coroutines.launch

const val MAIN_SCREEN_ROUTE = "mainScreen"

@Composable
fun MainScreen() {
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState()

    if (LocalContext.current.screenIsLand) {
        Row(modifier = Modifier.fillMaxSize()) {
            NavigationBarOrRail(
                currentPage = pagerState.currentPage,
                scrollToPage = {
                    coroutineScope.launch {
                        pagerState.scrollToPage(it)
                    }
                }
            )
            Box(modifier = Modifier.weight(1f)) {
                ContentPager(pagerState = pagerState)
            }
        }
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f)) {
                ContentPager(pagerState = pagerState)
            }
            NavigationBarOrRail(
                currentPage = pagerState.currentPage,
                scrollToPage = {
                    coroutineScope.launch {
                        pagerState.scrollToPage(it)
                    }
                }
            )
        }
    }
}

@Composable
private fun NavigationBarOrRail(
    currentPage: Int,
    scrollToPage: (Int) -> Unit
) {
    val items = listOf(
        stringResource(R.string.navi_bar_home), stringResource(R.string.navi_bar_more)
    )
    val icons = listOf(
        Icons.Default.Home, Icons.Default.Egg
    )

    if (LocalContext.current.screenIsLand) {
        NavigationRail {
            items.forEachIndexed { index, item ->
                NavigationRailItem(
                    icon = { Icon(icons[index], contentDescription = item) },
                    label = { Text(item) },
                    selected = currentPage == index,
                    onClick = { scrollToPage(index) }
                )
            }
        }
    } else {
        NavigationBar {
            items.forEachIndexed { index, item ->
                NavigationBarItem(
                    icon = { Icon(icons[index], contentDescription = item) },
                    label = { Text(item) },
                    selected = currentPage == index,
                    onClick = { scrollToPage(index) }
                )
            }
        }
    }
}

@Composable
private fun ContentPager(pagerState: PagerState) {
    Column {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            pageCount = 2,
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
}