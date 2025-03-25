package com.skyd.rays.ui.screen.settings.ml.classification

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DataThresholding
import androidx.compose.material.icons.outlined.ModelTraining
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.skyd.rays.R
import com.skyd.rays.model.preference.ai.ClassificationThresholdPreference
import com.skyd.rays.model.preference.ai.UseClassificationInAddPreference
import com.skyd.rays.ui.component.BaseSettingsItem
import com.skyd.rays.ui.component.RaysTopBar
import com.skyd.rays.ui.component.RaysTopBarStyle
import com.skyd.rays.ui.component.SliderSettingsItem
import com.skyd.rays.ui.component.SwitchSettingsItem
import com.skyd.rays.ui.component.TipSettingsItem
import com.skyd.rays.ui.local.LocalClassificationThreshold
import com.skyd.rays.ui.local.LocalNavController
import com.skyd.rays.ui.local.LocalUseClassificationInAdd
import com.skyd.rays.ui.screen.settings.ml.classification.model.ClassificationModelRoute
import kotlinx.serialization.Serializable


@Serializable
data object ClassificationRoute

@Composable
fun ClassificationScreen() {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val navController = LocalNavController.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

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
                    painter = rememberVectorPainter(image = Icons.Outlined.ModelTraining),
                    text = stringResource(id = R.string.classification_model_screen_name),
                    descriptionText = stringResource(
                        R.string.classification_model_screen_description,
                    ),
                    onClick = { navController.navigate(ClassificationModelRoute) }
                )
            }
            item { ClassificationThresholdSettingItem() }
            item {
                SwitchSettingsItem(
                    imageVector = null,
                    text = stringResource(R.string.classification_model_screen_enable_on_add_screen),
                    description = stringResource(R.string.classification_model_screen_enable_on_add_screen_description),
                    checked = LocalUseClassificationInAdd.current,
                    onCheckedChange = { UseClassificationInAddPreference.put(context, scope, it) },
                )
            }
            item { TipSettingsItem(text = stringResource(R.string.classification_screen_threshold_description)) }
        }
    }
}

@Composable
private fun ClassificationThresholdSettingItem() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    SliderSettingsItem(
        imageVector = Icons.Outlined.DataThresholding,
        text = stringResource(id = R.string.classification_screen_threshold),
        value = LocalClassificationThreshold.current,
        onValueChange = {
            ClassificationThresholdPreference.put(
                context = context,
                scope = scope,
                value = it,
            )
        },
    )
}