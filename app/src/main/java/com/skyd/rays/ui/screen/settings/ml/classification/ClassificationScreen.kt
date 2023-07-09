package com.skyd.rays.ui.screen.settings.ml.classification

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DataThresholding
import androidx.compose.material.icons.filled.ModelTraining
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.skyd.rays.R
import com.skyd.rays.model.preference.ai.ClassificationThresholdPreference
import com.skyd.rays.ui.component.BaseSettingsItem
import com.skyd.rays.ui.component.RaysTopBar
import com.skyd.rays.ui.component.RaysTopBarStyle
import com.skyd.rays.ui.component.TipSettingsItem
import com.skyd.rays.ui.local.LocalClassificationThreshold
import com.skyd.rays.ui.local.LocalNavController
import com.skyd.rays.ui.screen.settings.ml.classification.model.CLASSIFICATION_MODEL_SCREEN_ROUTE

const val CLASSIFICATION_SCREEN_ROUTE = "classificationScreen"

@Composable
fun ClassificationScreen() {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val navController = LocalNavController.current

    Scaffold(
        topBar = {
            RaysTopBar(
                style = RaysTopBarStyle.Large,
                scrollBehavior = scrollBehavior,
                title = { Text(text = stringResource(R.string.classification_screen_name)) },
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
                    icon = rememberVectorPainter(image = Icons.Default.ModelTraining),
                    text = stringResource(id = R.string.classification_model_screen_name),
                    descriptionText = stringResource(
                        R.string.classification_model_screen_description,
                    ),
                    onClick = { navController.navigate(CLASSIFICATION_MODEL_SCREEN_ROUTE) }
                )
            }
            item { ClassificationThresholdSettingItem() }
            item { TipSettingsItem(text = stringResource(R.string.classification_screen_threshold_description)) }
        }
    }
}

@Composable
private fun ClassificationThresholdSettingItem() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val threshold = LocalClassificationThreshold.current

    BaseSettingsItem(
        icon = rememberVectorPainter(image = Icons.Default.DataThresholding),
        text = stringResource(id = R.string.classification_screen_threshold),
        description = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Slider(
                    modifier = Modifier.weight(1f),
                    value = threshold,
                    valueRange = 0f..1f,
                    onValueChange = {
                        ClassificationThresholdPreference.put(
                            context = context,
                            scope = scope,
                            value = it,
                        )
                    },
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = String.format("%.2f", threshold))
            }
        }
    )
}