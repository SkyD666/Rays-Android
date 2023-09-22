package com.skyd.rays.ui.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.skyd.rays.R
import com.skyd.rays.ext.popBackStackWithLifecycle
import com.skyd.rays.ui.local.LocalNavController

enum class RaysTopBarStyle {
    Small, Large, CenterAligned
}

@Composable
fun RaysTopBar(
    style: RaysTopBarStyle = RaysTopBarStyle.Small,
    title: @Composable () -> Unit,
    contentPadding: @Composable () -> PaddingValues = { PaddingValues() },
    navigationIcon: @Composable () -> Unit = { BackIcon() },
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
    actions: @Composable RowScope.() -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    val colors = when (style) {
        RaysTopBarStyle.Small -> TopAppBarDefaults.topAppBarColors()
        RaysTopBarStyle.Large -> TopAppBarDefaults.largeTopAppBarColors()
        RaysTopBarStyle.CenterAligned -> TopAppBarDefaults.centerAlignedTopAppBarColors()
    }
    val topBarModifier = Modifier.padding(contentPadding())
    when (style) {
        RaysTopBarStyle.Small -> {
            TopAppBar(
                title = title,
                modifier = topBarModifier,
                navigationIcon = navigationIcon,
                actions = actions,
                windowInsets = windowInsets,
                colors = colors,
                scrollBehavior = scrollBehavior
            )
        }

        RaysTopBarStyle.Large -> {
            LargeTopAppBar(
                modifier = topBarModifier,
                title = title,
                navigationIcon = navigationIcon,
                actions = actions,
                windowInsets = windowInsets,
                colors = colors,
                scrollBehavior = scrollBehavior
            )
        }

        RaysTopBarStyle.CenterAligned -> {
            CenterAlignedTopAppBar(
                modifier = topBarModifier,
                title = title,
                navigationIcon = navigationIcon,
                actions = actions,
                windowInsets = windowInsets,
                colors = colors,
                scrollBehavior = scrollBehavior
            )
        }
    }
}

@Composable
fun TopBarIcon(
    painter: Painter,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    tint: Color = LocalContentColor.current,
    contentDescription: String?,
) {
    RaysIconButton(
        modifier = modifier,
        painter = painter,
        tint = tint,
        contentDescription = contentDescription,
        onClick = onClick
    )
}

@Composable
fun TopBarIcon(
    imageVector: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    tint: Color = LocalContentColor.current,
    contentDescription: String?,
) {
    RaysIconButton(
        modifier = modifier,
        imageVector = imageVector,
        tint = tint,
        contentDescription = contentDescription,
        onClick = onClick
    )
}

@Composable
fun BackIcon() {
    val navController = LocalNavController.current
    BackIcon {
        navController.popBackStackWithLifecycle()
    }
}

@Composable
fun BackIcon(onClick: () -> Unit = {}) {
    TopBarIcon(
        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
        contentDescription = stringResource(id = R.string.back),
        onClick = onClick
    )
}
