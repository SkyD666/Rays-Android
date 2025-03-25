package com.skyd.rays.ui.screen.more

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ImportExport
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
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
import com.skyd.rays.ui.component.shape.CloverShape
import com.skyd.rays.ui.component.shape.CurlyCornerShape
import com.skyd.rays.ui.component.shape.SquircleShape
import com.skyd.rays.ui.local.LocalNavController
import com.skyd.rays.ui.screen.about.AboutRoute
import com.skyd.rays.ui.screen.settings.SettingsRoute
import com.skyd.rays.ui.screen.settings.data.importexport.ImportExportRoute
import kotlinx.serialization.Serializable


@Serializable
data object MoreRoute

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
                        imageVector = Icons.Outlined.Widgets,
                        onClick = {
                            snackbarHostState.showSnackbar(
                                message = "\uD83C\uDFEE Happy New Year 2025~",
                                scope = scope,
                                withDismissAction = true
                            )
                        }
                    )
                },
            )
        },
    ) {
        val colorScheme: ColorScheme = MaterialTheme.colorScheme
        val dataList = remember(context, colorScheme, density, navController) {
            getMoreList(context, colorScheme, density, navController)
        }
        LazyVerticalGrid(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = it + PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            columns = GridCells.Adaptive(130.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(dataList) { item ->
                More1Item(
                    data = item,
                    onClickListener = { data -> data.action.invoke() }
                )
            }
        }
    }
}

@Composable
private fun More1Item(
    data: More1Bean,
    onClickListener: ((data: More1Bean) -> Unit)? = null
) {
    OutlinedCard(shape = RoundedCornerShape(16)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = {
                        onClickListener?.invoke(data)
                    }
                )
                .padding(25.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .padding(5.dp)
                    .background(
                        color = data.shapeColor,
                        shape = data.shape
                    )
                    .padding(16.dp)
            ) {
                Icon(
                    modifier = Modifier.size(35.dp),
                    imageVector = data.icon,
                    contentDescription = null,
                    tint = data.iconTint
                )
            }
            Text(
                modifier = Modifier
                    .padding(horizontal = 5.dp)
                    .padding(top = 15.dp)
                    .basicMarquee(iterations = Int.MAX_VALUE),
                text = data.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                textAlign = TextAlign.Center
            )
        }
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
            icon = Icons.Outlined.ImportExport,
            iconTint = colorScheme.onPrimary,
            shape = SquircleShape,
            shapeColor = colorScheme.primary,
            action = { navController.navigate(ImportExportRoute) }
        ),
        More1Bean(
            title = context.getString(R.string.settings),
            icon = Icons.Outlined.Settings,
            iconTint = colorScheme.onSecondary,
            shape = CloverShape,
            shapeColor = colorScheme.secondary,
            action = { navController.navigate(SettingsRoute) }
        ),
        More1Bean(
            title = context.getString(R.string.about),
            icon = Icons.Outlined.Info,
            iconTint = colorScheme.onTertiary,
            shape = CurlyCornerShape(amp = with(density) { 2.dp.toPx() }, count = 10),
            shapeColor = colorScheme.tertiary,
            action = { navController.navigate(AboutRoute) }
        )
    )
}