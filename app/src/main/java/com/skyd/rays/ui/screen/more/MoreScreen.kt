package com.skyd.rays.ui.screen.more

import android.content.Context
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ImportExport
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.skyd.rays.R
import com.skyd.rays.ext.plus
import com.skyd.rays.ext.showSnackbar
import com.skyd.rays.model.bean.More1Bean
import com.skyd.rays.ui.component.RaysIconButton
import com.skyd.rays.ui.component.RaysTopBar
import com.skyd.rays.ui.component.RaysTopBarStyle
import com.skyd.rays.ui.component.lazyverticalgrid.RaysLazyVerticalGrid
import com.skyd.rays.ui.component.lazyverticalgrid.adapter.LazyGridAdapter
import com.skyd.rays.ui.component.lazyverticalgrid.adapter.proxy.More1Proxy
import com.skyd.rays.ui.component.shape.CloverShape
import com.skyd.rays.ui.component.shape.CurlyCornerShape
import com.skyd.rays.ui.component.shape.SquircleShape
import com.skyd.rays.ui.local.LocalNavController
import com.skyd.rays.ui.screen.about.ABOUT_SCREEN_ROUTE
import com.skyd.rays.ui.screen.settings.SETTINGS_SCREEN_ROUTE
import com.skyd.rays.ui.screen.settings.data.importexport.IMPORT_EXPORT_SCREEN_ROUTE

const val MORE_SCREEN_ROUTE = "moreScreen"

@Composable
fun MoreScreen() {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val navController = LocalNavController.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val density = LocalDensity.current

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            RaysTopBar(
                style = RaysTopBarStyle.Large,
                scrollBehavior = scrollBehavior,
                title = { Text(text = stringResource(id = R.string.more_screen_name)) },
                navigationIcon = {
                    RaysIconButton(
                        imageVector = Icons.Default.Widgets,
                        onClick = {
                            snackbarHostState.showSnackbar(
                                message = "\uD83C\uDFEE Happy New Year 2024~",
                                scope = scope,
                                withDismissAction = true
                            )
                        }
                    )
                },
            )
        },
    ) {
        val adapter = remember {
            LazyGridAdapter(
                mutableListOf(
                    More1Proxy(onClickListener = { data ->
                        data.action.invoke()
                    })
                )
            )
        }
        val colorScheme: ColorScheme = MaterialTheme.colorScheme
        RaysLazyVerticalGrid(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            dataList = remember(context, colorScheme, density, navController) {
                getMoreList(context, colorScheme, density, navController)
            },
            adapter = adapter,
            contentPadding = it + PaddingValues(vertical = 10.dp),
        )
    }
}

private fun getMoreList(
    context: Context,
    colorScheme: ColorScheme,
    density: Density,
    navController: NavController,
): List<More1Bean> {
    return listOf(
        More1Bean(
            title = context.getString(R.string.import_export_screen_name),
            icon = Icons.Default.ImportExport,
            iconTint = colorScheme.onPrimary,
            shape = SquircleShape,
            shapeColor = colorScheme.primary,
            action = { navController.navigate(IMPORT_EXPORT_SCREEN_ROUTE) }
        ),
        More1Bean(
            title = context.getString(R.string.settings),
            icon = Icons.Default.Settings,
            iconTint = colorScheme.onSecondary,
            shape = CloverShape,
            shapeColor = colorScheme.secondary,
            action = { navController.navigate(SETTINGS_SCREEN_ROUTE) }
        ),
        More1Bean(
            title = context.getString(R.string.about),
            icon = Icons.Default.Info,
            iconTint = colorScheme.onTertiary,
            shape = CurlyCornerShape(amp = with(density) { 2.dp.toPx() }, count = 10),
            shapeColor = colorScheme.tertiary,
            action = { navController.navigate(ABOUT_SCREEN_ROUTE) }
        )
    )
}