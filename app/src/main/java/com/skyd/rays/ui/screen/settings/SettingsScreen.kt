package com.skyd.rays.ui.screen.settings

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ManageSearch
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Api
import androidx.compose.material.icons.outlined.Dataset
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import com.skyd.rays.R
import com.skyd.rays.ui.component.BaseSettingsItem
import com.skyd.rays.ui.component.RaysTopBar
import com.skyd.rays.ui.component.RaysTopBarStyle
import com.skyd.rays.ui.local.LocalNavController
import com.skyd.rays.ui.screen.settings.api.ApiRoute
import com.skyd.rays.ui.screen.settings.appearance.AppearanceRoute
import com.skyd.rays.ui.screen.settings.data.DataRoute
import com.skyd.rays.ui.screen.settings.ml.MlRoute
import com.skyd.rays.ui.screen.settings.privacy.PrivacyRoute
import com.skyd.rays.ui.screen.settings.searchconfig.SearchConfigRoute
import com.skyd.rays.ui.screen.settings.shareconfig.ShareConfigRoute
import kotlinx.serialization.Serializable


@Serializable
data object SettingsRoute

@Composable
fun SettingsScreen() {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val navController = LocalNavController.current
    Scaffold(
        topBar = {
            RaysTopBar(
                style = RaysTopBarStyle.Large,
                scrollBehavior = scrollBehavior,
                title = { Text(text = stringResource(R.string.settings)) },
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = paddingValues,
        ) {
            item {
                BaseSettingsItem(
                    painter = rememberVectorPainter(Icons.Outlined.Palette),
                    text = stringResource(id = R.string.appearance_screen_name),
                    descriptionText = stringResource(id = R.string.setting_screen_appearance_description),
                    onClick = { navController.navigate(AppearanceRoute) }
                )
            }
            item {
                BaseSettingsItem(
                    painter = rememberVectorPainter(Icons.AutoMirrored.Outlined.ManageSearch),
                    text = stringResource(id = R.string.search_config_screen_name),
                    descriptionText = stringResource(id = R.string.setting_screen_search_description),
                    onClick = { navController.navigate(SearchConfigRoute) }
                )
            }
            item {
                BaseSettingsItem(
                    painter = rememberVectorPainter(Icons.Outlined.Share),
                    text = stringResource(id = R.string.share_config_screen_name),
                    descriptionText = stringResource(id = R.string.setting_screen_share_config_description),
                    onClick = { navController.navigate(ShareConfigRoute) }
                )
            }
            item {
                BaseSettingsItem(
                    painter = rememberVectorPainter(Icons.Outlined.Dataset),
                    text = stringResource(id = R.string.data_screen_name),
                    descriptionText = stringResource(id = R.string.setting_screen_data_description),
                    onClick = { navController.navigate(DataRoute) }
                )
            }
            item {
                BaseSettingsItem(
                    painter = rememberVectorPainter(Icons.Outlined.SmartToy),
                    text = stringResource(id = R.string.ml_screen_name),
                    descriptionText = stringResource(id = R.string.setting_screen_ml_description),
                    onClick = { navController.navigate(MlRoute) }
                )
            }
            item {
                BaseSettingsItem(
                    painter = rememberVectorPainter(Icons.Outlined.AdminPanelSettings),
                    text = stringResource(id = R.string.privacy_screen_name),
                    descriptionText = stringResource(id = R.string.setting_screen_privacy_description),
                    onClick = { navController.navigate(PrivacyRoute) }
                )
            }
            item {
                BaseSettingsItem(
                    painter = rememberVectorPainter(Icons.Outlined.Api),
                    text = stringResource(id = R.string.api_screen_name),
                    descriptionText = stringResource(id = R.string.setting_screen_api_description),
                    onClick = { navController.navigate(ApiRoute) }
                )
            }
        }
    }
}
