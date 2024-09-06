package com.skyd.rays.ui.screen.minitool

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material.icons.outlined.PeopleAlt
import androidx.compose.material.icons.outlined.Style
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.skyd.rays.R
import com.skyd.rays.ext.plus
import com.skyd.rays.ext.showSnackbar
import com.skyd.rays.model.bean.MiniTool1Bean
import com.skyd.rays.ui.component.RaysIconButton
import com.skyd.rays.ui.component.RaysTopBar
import com.skyd.rays.ui.component.RaysTopBarStyle
import com.skyd.rays.ui.component.lazyverticalgrid.RaysLazyVerticalGrid
import com.skyd.rays.ui.component.lazyverticalgrid.adapter.LazyGridAdapter
import com.skyd.rays.ui.component.lazyverticalgrid.adapter.proxy.MiniTool1Proxy
import com.skyd.rays.ui.local.LocalNavController
import com.skyd.rays.ui.screen.minitool.selfiesegmentation.SELFIE_SEGMENTATION_SCREEN_ROUTE
import com.skyd.rays.ui.screen.minitool.styletransfer.STYLE_TRANSFER_SCREEN_ROUTE

const val MINI_TOOL_SCREEN_ROUTE = "miniToolScreen"

@Composable
fun MiniToolScreen() {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val navController = LocalNavController.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            RaysTopBar(
                style = RaysTopBarStyle.Large,
                scrollBehavior = scrollBehavior,
                title = { Text(text = stringResource(id = R.string.mini_tool_screen_name)) },
                navigationIcon = {
                    RaysIconButton(
                        imageVector = Icons.Outlined.Extension,
                        onClick = {
                            snackbarHostState.showSnackbar(
                                message = "\ud83c\udfee Happy New Year 2024~",
                                scope = scope,
                                withDismissAction = true
                            )
                        }
                    )
                },
            )
        },
    ) {
        val miniToolList = listOf(
            MiniTool1Bean(
                title = stringResource(R.string.style_transfer_screen_name),
                icon = Icons.Outlined.Style,
                action = { navController.navigate(STYLE_TRANSFER_SCREEN_ROUTE) }
            ),
            MiniTool1Bean(
                title = stringResource(R.string.selfie_segmentation_screen_name),
                icon = Icons.Outlined.PeopleAlt,
                action = { navController.navigate(SELFIE_SEGMENTATION_SCREEN_ROUTE) }
            ),
        )

        val adapter = remember {
            LazyGridAdapter(
                mutableListOf(MiniTool1Proxy(onClickListener = { data -> data.action.invoke() }))
            )
        }
        RaysLazyVerticalGrid(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            dataList = miniToolList,
            adapter = adapter,
            contentPadding = it + PaddingValues(vertical = 10.dp),
        )
    }
}