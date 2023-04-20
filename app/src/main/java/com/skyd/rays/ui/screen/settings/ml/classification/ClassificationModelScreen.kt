package com.skyd.rays.ui.screen.settings.ml.classification

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.ModelTraining
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.skyd.rays.R
import com.skyd.rays.ui.component.BaseSettingsItem
import com.skyd.rays.ui.component.RaysTopBar
import com.skyd.rays.ui.component.RaysTopBarStyle


const val CLASSIFICATION_MODEL_SCREEN_ROUTE = "classificationModelScreen"

@Composable
fun ClassificationModelScreen() {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            RaysTopBar(
                style = RaysTopBarStyle.Large,
                scrollBehavior = scrollBehavior,
                title = { Text(text = stringResource(R.string.classification_model_screen_name)) },
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection), contentPadding = paddingValues
        ) {
            item {
                BaseSettingsItem(
                    icon = rememberVectorPainter(image = Icons.Default.ModelTraining),
                    text = stringResource(id = R.string.classification_model_screen_default),
                    descriptionText = null,
                )
            }
            item {
                BaseSettingsItem(
                    icon = rememberVectorPainter(image = Icons.Default.CreateNewFolder),
                    text = stringResource(id = R.string.classification_model_screen_select),
                    descriptionText = stringResource(
                        R.string.classification_model_screen_select_description,
                    )
                )
            }
        }
    }
}
