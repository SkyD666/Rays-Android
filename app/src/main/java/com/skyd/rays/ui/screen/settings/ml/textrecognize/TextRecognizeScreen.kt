package com.skyd.rays.ui.screen.settings.ml.textrecognize

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DataThresholding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.skyd.compone.component.ComponeTopBar
import com.skyd.compone.component.ComponeTopBarStyle
import com.skyd.rays.R
import com.skyd.rays.model.preference.ai.TextRecognizeThresholdPreference
import com.skyd.rays.model.preference.ai.UseTextRecognizeInAddPreference
import com.skyd.rays.ui.local.LocalTextRecognizeThreshold
import com.skyd.rays.ui.local.LocalUseTextRecognizeInAdd
import com.skyd.settings.SettingsLazyColumn
import com.skyd.settings.SliderSettingsItem
import com.skyd.settings.SwitchSettingsItem
import com.skyd.settings.TipSettingsItem
import com.skyd.settings.dsl.SettingsBaseItemScope
import kotlinx.serialization.Serializable


@Serializable
data object TextRecognizeRoute

@Composable
fun TextRecognizeScreen() {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            ComponeTopBar(
                style = ComponeTopBarStyle.LargeFlexible,
                scrollBehavior = scrollBehavior,
                title = { Text(text = stringResource(R.string.text_recognize_screen_name)) },
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
                item { TextRecognizeThresholdSettingItem() }
                item {
                    SwitchSettingsItem(
                        imageVector = null,
                        text = stringResource(R.string.text_recognize_model_screen_enable_on_add_screen),
                        description = stringResource(R.string.text_recognize_screen_enable_on_add_screen_description),
                        checked = LocalUseTextRecognizeInAdd.current,
                        onCheckedChange = {
                            UseTextRecognizeInAddPreference.put(context, scope, it)
                        },
                    )
                }
            }
            item { TipSettingsItem(text = stringResource(R.string.text_recognize_screen_threshold_description)) }
        }
    }
}

@Composable
private fun SettingsBaseItemScope.TextRecognizeThresholdSettingItem() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    SliderSettingsItem(
        imageVector = Icons.Outlined.DataThresholding,
        text = stringResource(id = R.string.text_recognize_screen_threshold),
        value = LocalTextRecognizeThreshold.current,
        onValueChange = { TextRecognizeThresholdPreference.put(context, scope, it) },
        labelFormatter = { "%.2f".format(it) },
    )
}