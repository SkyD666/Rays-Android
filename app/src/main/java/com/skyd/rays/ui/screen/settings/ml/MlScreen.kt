package com.skyd.rays.ui.screen.settings.ml

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.TextFields
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
import com.skyd.rays.ui.screen.settings.ml.classification.CLASSIFICATION_SCREEN_ROUTE
import com.skyd.rays.ui.screen.settings.ml.textrecognize.TEXT_RECOGNIZE_SCREEN_ROUTE


const val ML_SCREEN_ROUTE = "mlScreen"

@Composable
fun MlScreen() {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val navController = LocalNavController.current

    Scaffold(
        topBar = {
            RaysTopBar(
                style = RaysTopBarStyle.Large,
                scrollBehavior = scrollBehavior,
                title = { Text(text = stringResource(R.string.ml_screen_name)) },
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
                    painter = rememberVectorPainter(image = Icons.Default.Sell),
                    text = stringResource(id = R.string.classification_screen_name),
                    descriptionText = stringResource(R.string.classification_screen_description),
                    onClick = { navController.navigate(CLASSIFICATION_SCREEN_ROUTE) }
                )
            }
            item {
                BaseSettingsItem(
                    painter = rememberVectorPainter(image = Icons.Default.TextFields),
                    text = stringResource(id = R.string.text_recognize_screen_name),
                    descriptionText = stringResource(R.string.text_recognize_screen_description),
                    onClick = { navController.navigate(TEXT_RECOGNIZE_SCREEN_ROUTE) }
                )
            }
        }
    }
}
