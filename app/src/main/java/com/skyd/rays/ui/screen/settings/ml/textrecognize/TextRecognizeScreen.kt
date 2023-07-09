package com.skyd.rays.ui.screen.settings.ml.textrecognize

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DataThresholding
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
import com.skyd.rays.model.preference.ai.TextRecognizeThresholdPreference
import com.skyd.rays.ui.component.BaseSettingsItem
import com.skyd.rays.ui.component.RaysTopBar
import com.skyd.rays.ui.component.RaysTopBarStyle
import com.skyd.rays.ui.component.TipSettingsItem
import com.skyd.rays.ui.local.LocalTextRecognizeThreshold

const val TEXT_RECOGNIZE_SCREEN_ROUTE = "textRecognizeScreen"

@Composable
fun TextRecognizeScreen() {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        topBar = {
            RaysTopBar(
                style = RaysTopBarStyle.Large,
                scrollBehavior = scrollBehavior,
                title = { Text(text = stringResource(R.string.text_recognize_screen_name)) },
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = paddingValues,
        ) {
            item { TextRecognizeThresholdSettingItem() }
            item { TipSettingsItem(text = stringResource(R.string.text_recognize_screen_threshold_description)) }
        }
    }
}

@Composable
private fun TextRecognizeThresholdSettingItem() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val threshold = LocalTextRecognizeThreshold.current

    BaseSettingsItem(
        icon = rememberVectorPainter(image = Icons.Default.DataThresholding),
        text = stringResource(id = R.string.text_recognize_screen_threshold),
        description = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Slider(
                    modifier = Modifier.weight(1f),
                    value = threshold,
                    valueRange = 0f..1f,
                    onValueChange = {
                        TextRecognizeThresholdPreference.put(
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