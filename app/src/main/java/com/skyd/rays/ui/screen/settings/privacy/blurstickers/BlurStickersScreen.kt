package com.skyd.rays.ui.screen.settings.privacy.blurstickers

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.BlurLinear
import androidx.compose.material.icons.outlined.BlurOn
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.skyd.rays.R
import com.skyd.rays.model.preference.privacy.BlurStickerKeywordsPreference
import com.skyd.rays.model.preference.privacy.BlurStickerPreference
import com.skyd.rays.model.preference.privacy.BlurStickerRadiusPreference
import com.skyd.rays.ui.component.BannerItem
import com.skyd.rays.ui.component.BaseSettingsItem
import com.skyd.rays.ui.component.RaysIconButton
import com.skyd.rays.ui.component.RaysTopBar
import com.skyd.rays.ui.component.RaysTopBarStyle
import com.skyd.rays.ui.component.SliderSettingsItem
import com.skyd.rays.ui.component.SwitchSettingsItem
import com.skyd.rays.ui.component.TipSettingsItem
import com.skyd.rays.ui.component.dialog.TextFieldDialog
import com.skyd.rays.ui.local.LocalBlurSticker
import com.skyd.rays.ui.local.LocalBlurStickerKeywords
import com.skyd.rays.ui.local.LocalBlurStickerRadius
import kotlinx.serialization.Serializable


@Serializable
data object BlurStickersRoute

@Composable
fun BlurStickersScreen() {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            RaysTopBar(
                style = RaysTopBarStyle.Large,
                scrollBehavior = scrollBehavior,
                title = { Text(text = stringResource(R.string.blur_stickers_screen_name)) },
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
                BannerItem {
                    SwitchSettingsItem(
                        imageVector = Icons.Outlined.BlurOn,
                        checked = LocalBlurSticker.current,
                        text = stringResource(R.string.enable),
                        onCheckedChange = {
                            BlurStickerPreference.put(
                                context = context,
                                scope = scope,
                                value = it
                            )
                        }
                    )
                }
            }
            item { BlurStickerRadiusSettingItem() }
            item { BlurStickersKeywordsSettingItem() }
            item {
                TipSettingsItem(
                    text = stringResource(id = R.string.blur_stickers_screen_tip)
                )
            }
        }
    }
}

@Composable
private fun BlurStickersKeywordsSettingItem() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var openAddDialog by rememberSaveable { mutableStateOf(false) }
    var addDialogText by rememberSaveable { mutableStateOf("") }
    val blurStickersKeywords = LocalBlurStickerKeywords.current

    BaseSettingsItem(
        painter = rememberVectorPainter(Icons.Outlined.Tag),
        text = stringResource(R.string.blur_stickers_screen_blur_keywords),
        enabled = LocalBlurSticker.current,
        content = {
            RaysIconButton(
                enabled = LocalBlurSticker.current,
                onClick = { openAddDialog = true },
                imageVector = Icons.Outlined.Add,
            )
        },
        description = {
            FlowRow(
                modifier = Modifier.animateContentSize(),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                repeat(blurStickersKeywords.size) { index ->
                    InputChip(
                        selected = false,
                        enabled = LocalBlurSticker.current,
                        label = { Text(blurStickersKeywords.elementAt(index)) },
                        onClick = {
                            BlurStickerKeywordsPreference.put(
                                context = context,
                                scope = scope,
                                value = blurStickersKeywords -
                                        blurStickersKeywords.elementAt(index),
                            )
                        },
                        trailingIcon = {
                            Icon(
                                modifier = Modifier.size(AssistChipDefaults.IconSize),
                                imageVector = Icons.Outlined.Close,
                                contentDescription = null
                            )
                        }
                    )
                }
            }
        },
    )

    TextFieldDialog(
        visible = openAddDialog,
        title = stringResource(id = R.string.blur_stickers_screen_add_blur_keyword),
        maxLines = 1,
        onDismissRequest = { openAddDialog = false },
        value = addDialogText,
        onValueChange = { addDialogText = it },
        onConfirm = {
            BlurStickerKeywordsPreference.put(
                context = context,
                scope = scope,
                value = blurStickersKeywords + it,
            )
            addDialogText = ""
            openAddDialog = false
        }
    )
}

@Composable
private fun BlurStickerRadiusSettingItem() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    SliderSettingsItem(
        imageVector = Icons.Outlined.BlurLinear,
        text = stringResource(id = R.string.blur_stickers_screen_radius),
        enabled = LocalBlurSticker.current,
        valueRange = 0.01f..25f,
        value = LocalBlurStickerRadius.current,
        onValueChange = {
            BlurStickerRadiusPreference.put(
                context = context,
                scope = scope,
                value = it,
            )
        },
    )
}