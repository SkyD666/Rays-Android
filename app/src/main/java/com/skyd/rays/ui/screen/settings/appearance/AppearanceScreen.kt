package com.skyd.rays.ui.screen.settings.appearance

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Contrast
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.edit
import com.materialkolor.ktx.from
import com.materialkolor.ktx.toneColor
import com.materialkolor.palettes.CorePalette
import com.materialkolor.palettes.TonalPalette
import com.skyd.compone.component.ComponeTopBar
import com.skyd.compone.component.ComponeTopBarStyle
import com.skyd.compone.component.dialog.TextFieldDialog
import com.skyd.compone.local.LocalNavController
import com.skyd.rays.R
import com.skyd.rays.ext.checkColorHex
import com.skyd.rays.ext.dataStore
import com.skyd.rays.ext.onDark
import com.skyd.rays.ext.toColorOrNull
import com.skyd.rays.model.preference.theme.AmoledDarkModePreference
import com.skyd.rays.model.preference.theme.CustomPrimaryColorPreference
import com.skyd.rays.model.preference.theme.DarkModePreference
import com.skyd.rays.model.preference.theme.StickerColorThemePreference
import com.skyd.rays.model.preference.theme.ThemeNamePreference
import com.skyd.rays.ui.component.BlockRadioButton
import com.skyd.rays.ui.component.BlockRadioGroupButtonItem
import com.skyd.rays.ui.component.RadioTextItem
import com.skyd.rays.ui.local.LocalAmoledDarkMode
import com.skyd.rays.ui.local.LocalCustomPrimaryColor
import com.skyd.rays.ui.local.LocalDarkMode
import com.skyd.rays.ui.local.LocalStickerColorTheme
import com.skyd.rays.ui.local.LocalThemeName
import com.skyd.rays.ui.screen.settings.appearance.style.SearchStyleRoute
import com.skyd.rays.ui.theme.extractColors
import com.skyd.rays.ui.theme.extractColorsFromWallpaper
import com.skyd.settings.BaseSettingsItem
import com.skyd.settings.SettingsLazyColumn
import com.skyd.settings.SwitchSettingsItem
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable


@Serializable
data object AppearanceRoute

@Composable
fun AppearanceScreen() {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val navController = LocalNavController.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val themeName = LocalThemeName.current

    val tonalPalettes = extractColors(darkTheme = false)
    val tonalPalettesFromWallpaper = extractColorsFromWallpaper(darkTheme = false)
    var wallpaperOrBasicThemeSelected by rememberSaveable {
        mutableIntStateOf(if (tonalPalettesFromWallpaper.containsKey(themeName)) 0 else 1)
    }
    var openDarkBottomSheet by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            ComponeTopBar(
                style = ComponeTopBarStyle.LargeFlexible,
                scrollBehavior = scrollBehavior,
                title = { Text(text = stringResource(R.string.appearance_screen_name)) },
            )
        }
    ) { paddingValues ->
        SettingsLazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = paddingValues,
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                BlockRadioButton(
                    selected = wallpaperOrBasicThemeSelected,
                    onSelected = { wallpaperOrBasicThemeSelected = it },
                    itemRadioGroups = listOf(
                        BlockRadioGroupButtonItem(
                            text = stringResource(R.string.appearance_screen_wallpaper_colors),
                            onClick = {},
                        ) {
                            Palettes(colors = tonalPalettesFromWallpaper)
                        },
                        BlockRadioGroupButtonItem(
                            text = stringResource(R.string.appearance_screen_basic_colors),
                            onClick = {},
                        ) {
                            Palettes(colors = tonalPalettes)
                        },
                    ),
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            group {
                item {
                    BaseSettingsItem(
                        icon = rememberVectorPainter(image = Icons.Outlined.DarkMode),
                        text = stringResource(id = R.string.appearance_screen_dark_mode),
                        descriptionText = stringResource(id = R.string.appearance_screen_dark_mode_description),
                        onClick = { openDarkBottomSheet = true }
                    )
                }
                item {
                    SwitchSettingsItem(
                        imageVector = Icons.Outlined.Contrast,
                        text = stringResource(id = R.string.appearance_screen_amoled_dark),
                        checked = LocalAmoledDarkMode.current,
                        onCheckedChange = {
                            AmoledDarkModePreference.put(
                                context = context,
                                scope = scope,
                                value = it,
                            )
                        }
                    )
                }
                item {
                    SwitchSettingsItem(
                        imageVector = Icons.Outlined.Palette,
                        text = stringResource(R.string.appearance_screen_sticker_color_theme),
                        description = stringResource(R.string.appearance_screen_sticker_color_theme_description),
                        checked = LocalStickerColorTheme.current,
                        onCheckedChange = {
                            StickerColorThemePreference.put(
                                context = context,
                                scope = scope,
                                value = it,
                            )
                        },
                    )
                }
            }
            group(text = { context.getString(R.string.appearance_screen_style_category) }) {
                item {
                    BaseSettingsItem(
                        icon = rememberVectorPainter(image = Icons.Outlined.Search),
                        text = stringResource(id = R.string.search_style_screen_name),
                        descriptionText = null,
                        onClick = { navController.navigate(SearchStyleRoute) }
                    )
                }
            }
        }
        if (openDarkBottomSheet) {
            DarkModeSheet {
                openDarkBottomSheet = false
            }
        }
    }
}

@Composable
private fun DarkModeSheet(onDismissRequest: () -> Unit) {
    val bottomSheetState = rememberModalBottomSheetState()
    val darkMode = LocalDarkMode.current
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
            DarkModePreference.values.forEach {
                RadioTextItem(
                    text = DarkModePreference.toDisplayName(it),
                    selected = (it == darkMode),
                    onClick = {
                        DarkModePreference.put(
                            context = context, scope = scope,
                            value = it
                        )
                    },
                )
            }
        }
    }
}

@Composable
fun Palettes(
    colors: Map<String, ColorScheme>,
    themeName: String = LocalThemeName.current,
) {
    val customPrimaryColor = LocalCustomPrimaryColor.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var addDialogVisible by rememberSaveable { mutableStateOf(false) }
    var customColorValue by rememberSaveable { mutableStateOf(customPrimaryColor) }

    if (colors.isEmpty()) {
        Row(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .fillMaxWidth()
                .height(74.dp)
                .clip(RoundedCornerShape(16.dp))
                .clickable {},
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1)
                    stringResource(R.string.theme_no_palettes)
                else stringResource(R.string.only_android_8_1_plus),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.inverseSurface,
            )
        }
    } else {
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            colors.forEach { (t, u) ->
                val isCustom = ThemeNamePreference.isCustom(t)
                SelectableMiniPalette(
                    selected = t == themeName,
                    isCustom = isCustom,
                    onClick = {
                        if (isCustom) {
                            customColorValue = customPrimaryColor
                            addDialogVisible = true
                        } else {
                            ThemeNamePreference.put(context, scope, t)
                        }
                    },
                    accents = if (isCustom) {
                        val corePalette = CorePalette.of(
                            (customPrimaryColor.toColorOrNull() ?: Color.Transparent).toArgb()
                        )
                        listOf(corePalette.a1, corePalette.a2, corePalette.a3)
                    } else listOf(
                        TonalPalette.from(u.primary),
                        TonalPalette.from(u.secondary),
                        TonalPalette.from(u.tertiary)
                    )
                )
            }
        }
    }

    TextFieldDialog(
        visible = addDialogVisible,
        titleText = stringResource(R.string.primary_color),
        value = customColorValue,
        maxLines = 1,
        onValueChange = {
            customColorValue = it
        },
        onDismissRequest = {
            addDialogVisible = false
        },
        onConfirm = {
            it.checkColorHex()?.let { color ->
                scope.launch {
                    context.dataStore.edit { pref ->
                        pref[CustomPrimaryColorPreference.key] = color
                        pref[ThemeNamePreference.key] = ThemeNamePreference.CUSTOM_THEME_NAME
                    }
                }
                addDialogVisible = false
            }
        }
    )
}

@Composable
fun SelectableMiniPalette(
    selected: Boolean,
    isCustom: Boolean = false,
    onClick: () -> Unit,
    accents: List<TonalPalette>,
) {
    Box(
        modifier = Modifier
            .size(74.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
    ) {
        Box(
            modifier = Modifier
                .background(
                    if (isCustom) {
                        MaterialTheme.colorScheme.primaryContainer.copy(0.5f) onDark
                                MaterialTheme.colorScheme.onPrimaryContainer.copy(0.3f)
                    } else {
                        MaterialTheme.colorScheme.inverseOnSurface
                    },
                )
                .fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Surface(
                modifier = Modifier.size(50.dp),
                shape = CircleShape,
                color = accents[0].toneColor(36),
            ) {
                Box {
                    Surface(
                        modifier = Modifier
                            .size(50.dp)
                            .offset((-25).dp, 25.dp),
                        color = accents[1].toneColor(80),
                    ) {}
                    Surface(
                        modifier = Modifier
                            .size(50.dp)
                            .offset(25.dp, 25.dp),
                        color = accents[2].toneColor(65),
                    ) {}
                }
            }
        }
        AnimatedVisibility(
            visible = selected,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(
                        width = 2.dp,
                        color = accents[0].toneColor(50),
                        shape = RoundedCornerShape(16.dp),
                    )
                    .padding(2.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(15.dp),
                        ),
                )
            }
        }
    }
}