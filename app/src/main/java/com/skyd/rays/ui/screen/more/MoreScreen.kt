package com.skyd.rays.ui.screen.more

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.skyd.rays.R
import com.skyd.rays.ext.screenIsLand
import com.skyd.rays.model.bean.More1Bean
import com.skyd.rays.ui.component.RaysIconButton
import com.skyd.rays.ui.component.RaysTopBar
import com.skyd.rays.ui.component.lazyverticalgrid.RaysLazyVerticalGrid
import com.skyd.rays.ui.component.lazyverticalgrid.adapter.LazyGridAdapter
import com.skyd.rays.ui.component.lazyverticalgrid.adapter.proxy.More1Proxy
import com.skyd.rays.ui.component.shape.CurlyCornerShape
import com.skyd.rays.ui.component.shape.PolygonShape
import com.skyd.rays.ui.local.LocalNavController
import com.skyd.rays.ui.screen.about.ABOUT_SCREEN_ROUTE
import com.skyd.rays.ui.screen.minitool.MINI_TOOL_SCREEN_ROUTE
import com.skyd.rays.ui.screen.settings.SETTINGS_SCREEN_ROUTE
import com.skyd.rays.ui.screen.settings.data.importexport.IMPORT_EXPORT_SCREEN_ROUTE

@Composable
fun MoreScreen() {
    val navController = LocalNavController.current
    val context = LocalContext.current

    Scaffold(
        topBar = {
            RaysTopBar(
                title = { Text(text = stringResource(id = R.string.more_screen_name)) },
                navigationIcon = {
                    RaysIconButton(imageVector = Icons.Default.Egg, onClick = { })
                }
            )
        },
        contentWindowInsets = if (context.screenIsLand) {
            WindowInsets(
                left = 0,
                top = 0,
                right = ScaffoldDefaults.contentWindowInsets
                    .getRight(LocalDensity.current, LocalLayoutDirection.current),
                bottom = 0
            )
        } else {
            WindowInsets(0.dp)
        }
    ) {
        val moreList = listOf(
            More1Bean(
                title = stringResource(R.string.import_export_screen_name),
                icon = Icons.Default.ImportExport,
                iconTint = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(30),
                shapeColor = MaterialTheme.colorScheme.primary,
                action = { navController.navigate(IMPORT_EXPORT_SCREEN_ROUTE) }
            ),
            More1Bean(
                title = stringResource(R.string.mini_tool_screen_name),
                icon = Icons.Default.Extension,
                iconTint = MaterialTheme.colorScheme.onSecondary,
                shape = PolygonShape(10),
                shapeColor = MaterialTheme.colorScheme.secondary,
                action = { navController.navigate(MINI_TOOL_SCREEN_ROUTE) }
            ),
            More1Bean(
                title = stringResource(R.string.settings),
                icon = Icons.Default.Settings,
                iconTint = MaterialTheme.colorScheme.onSecondary,
                shape = CircleShape,
                shapeColor = MaterialTheme.colorScheme.secondary,
                action = { navController.navigate(SETTINGS_SCREEN_ROUTE) }
            ),
            More1Bean(
                title = stringResource(R.string.about),
                icon = Icons.Default.Info,
                iconTint = MaterialTheme.colorScheme.onTertiary,
                shape = CurlyCornerShape(
                    amp = with(LocalDensity.current) { 2.dp.toPx() },
                    count = 10
                ),
                shapeColor = MaterialTheme.colorScheme.tertiary,
                action = { navController.navigate(ABOUT_SCREEN_ROUTE) }
            )
        )

        val adapter = remember {
            LazyGridAdapter(
                mutableListOf(
                    More1Proxy(onClickListener = { data ->
                        data.action.invoke()
                    })
                )
            )
        }
        RaysLazyVerticalGrid(
            modifier = Modifier.fillMaxSize(),
            dataList = moreList,
            adapter = adapter,
            contentPadding = it
        )
    }
}