package com.skyd.rays.ui.screen.settings.api

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import com.skyd.compone.component.ComponeTopBar
import com.skyd.compone.component.ComponeTopBarStyle
import com.skyd.compone.local.LocalNavController
import com.skyd.rays.R
import com.skyd.rays.ui.screen.settings.api.apigrant.ApiGrantRoute
import com.skyd.settings.BaseSettingsItem
import com.skyd.settings.SettingsLazyColumn
import kotlinx.serialization.Serializable


@Serializable
data object ApiRoute

@Composable
fun ApiScreen() {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val navController = LocalNavController.current
    Scaffold(
        topBar = {
            ComponeTopBar(
                style = ComponeTopBarStyle.LargeFlexible,
                scrollBehavior = scrollBehavior,
                title = { Text(text = stringResource(R.string.api_screen_name)) },
            )
        }
    ) { paddingValues ->
        SettingsLazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = paddingValues,
        ) {
            group {
                item {
                    BaseSettingsItem(
                        icon = rememberVectorPainter(Icons.Outlined.Visibility),
                        text = stringResource(id = R.string.api_grant_screen_name),
                        descriptionText = stringResource(id = R.string.api_grant_screen_description),
                        onClick = { navController.navigate(ApiGrantRoute) }
                    )
                }
            }
        }
    }
}
