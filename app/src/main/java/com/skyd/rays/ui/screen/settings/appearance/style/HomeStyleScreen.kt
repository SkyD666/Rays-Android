package com.skyd.rays.ui.screen.settings.appearance.style

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.skyd.rays.R
import com.skyd.rays.model.preference.HomeShareButtonAlignmentPreference
import com.skyd.rays.model.preference.StickerScalePreference
import com.skyd.rays.ui.component.BaseSettingsItem
import com.skyd.rays.ui.component.CategorySettingsItem
import com.skyd.rays.ui.component.RaysIconButton
import com.skyd.rays.ui.component.RaysTopBar
import com.skyd.rays.ui.component.RaysTopBarStyle
import com.skyd.rays.ui.local.LocalHomeShareButtonAlignment
import com.skyd.rays.ui.local.LocalStickerScale
import com.skyd.rays.util.CommonUtil.openBrowser

const val HOME_STYLE_SCREEN_ROUTE = "homeStyleScreen"

@Composable
fun HomeStyleScreen() {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val stickerScale = LocalStickerScale.current
    var openStickerScaleSheet by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            RaysTopBar(
                style = RaysTopBarStyle.Large,
                scrollBehavior = scrollBehavior,
                title = { Text(text = stringResource(R.string.home_style_screen_name)) },
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection), contentPadding = paddingValues
        ) {
            item {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .padding(vertical = 12.dp)
                            .heightIn(max = 300.dp)
                            .wrapContentHeight()
                            .fillMaxWidth(0.8f),
                    ) {
                        HomeScreenPreview()
                    }
                }
            }
            item {
                CategorySettingsItem(text = stringResource(R.string.home_style_screen_sticker_card))
            }
            item {
                BaseSettingsItem(
                    icon = rememberVectorPainter(image = Icons.Default.AspectRatio),
                    text = stringResource(id = R.string.home_style_screen_sticker_scale),
                    descriptionText = StickerScalePreference.toDisplayName(stickerScale),
                    onClick = { openStickerScaleSheet = true }
                ) {
                    RaysIconButton(
                        onClick = { openBrowser("https://developer.android.google.cn/jetpack/compose/graphics/images/customize#content-scale") },
                        imageVector = Icons.Default.Help,
                    )
                }
            }
            item {
                HomeShareButtonAlignmentSettingItem()
            }
        }
        if (openStickerScaleSheet) {
            StickerScaleSheet {
                openStickerScaleSheet = false
            }
        }
    }
}

@Composable
private fun HomeShareButtonAlignmentSettingItem() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val homeShareButtonAlignment = LocalHomeShareButtonAlignment.current
    var horizontalSliderPosition by remember {
        mutableStateOf(homeShareButtonAlignment.horizontalBias)
    }
    var verticalSliderPosition by remember {
        mutableStateOf(homeShareButtonAlignment.verticalBias)
    }

    BaseSettingsItem(
        icon = rememberVectorPainter(image = Icons.Default.PictureInPicture),
        text = stringResource(id = R.string.home_style_screen_share_button_alignment),
        description = {
            Column {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = stringResource(R.string.home_style_screen_share_button_alignment_horiz))
                    Spacer(modifier = Modifier.width(6.dp))
                    Slider(
                        value = horizontalSliderPosition,
                        valueRange = -1f..1f,
                        onValueChange = {
                            horizontalSliderPosition = it
                            HomeShareButtonAlignmentPreference.put(
                                context = context,
                                scope = scope,
                                value = BiasAlignment(
                                    horizontalBias = it,
                                    verticalBias = verticalSliderPosition
                                )
                            )
                        },
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = stringResource(R.string.home_style_screen_share_button_alignment_vert))
                    Spacer(modifier = Modifier.width(6.dp))
                    Slider(
                        value = verticalSliderPosition,
                        valueRange = -1f..1f,
                        onValueChange = {
                            verticalSliderPosition = it
                            HomeShareButtonAlignmentPreference.put(
                                context = context,
                                scope = scope,
                                value = BiasAlignment(
                                    horizontalBias = horizontalSliderPosition,
                                    verticalBias = it
                                )
                            )
                        },
                    )
                }
            }
        },
    )
}

@Composable
private fun StickerScaleSheet(onDismissRequest: () -> Unit) {
    val bottomSheetState = rememberModalBottomSheetState()
    val stickerScale = LocalStickerScale.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = bottomSheetState,
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
                .selectableGroup()
        ) {
            StickerScalePreference.scaleList.forEach {
                Card(colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .selectable(
                                selected = (it == stickerScale),
                                onClick = {
                                    StickerScalePreference.put(
                                        context = context, scope = scope, value = it
                                    )
                                },
                                role = Role.RadioButton
                            )
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (it == stickerScale),
                            onClick = null // null recommended for accessibility with screen readers
                        )
                        Text(
                            text = StickerScalePreference.toDisplayName(it),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            }
        }
    }
}
