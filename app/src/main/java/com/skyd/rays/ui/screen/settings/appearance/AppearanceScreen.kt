package com.skyd.rays.ui.screen.settings.appearance

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
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
import com.materialkolor.palettes.CorePalette
import com.materialkolor.palettes.TonalPalette
import com.skyd.rays.R
import com.skyd.rays.ext.checkColorHex
import com.skyd.rays.ext.dataStore
import com.skyd.rays.ext.onDark
import com.skyd.rays.ext.toColorOrNull
import com.skyd.rays.model.preference.theme.CustomPrimaryColorPreference
import com.skyd.rays.model.preference.theme.DarkModePreference
import com.skyd.rays.model.preference.theme.StickerColorThemePreference
import com.skyd.rays.model.preference.theme.ThemeNamePreference
import com.skyd.rays.ui.component.BaseSettingsItem
import com.skyd.rays.ui.component.BlockRadioButton
import com.skyd.rays.ui.component.BlockRadioGroupButtonItem
import com.skyd.rays.ui.component.CategorySettingsItem
import com.skyd.rays.ui.component.RadioTextItem
import com.skyd.rays.ui.component.RaysTopBar
import com.skyd.rays.ui.component.RaysTopBarStyle
import com.skyd.rays.ui.component.SwitchSettingsItem
import com.skyd.rays.ui.component.dialog.TextFieldDialog
import com.skyd.rays.ui.local.LocalCustomPrimaryColor
import com.skyd.rays.ui.local.LocalDarkMode
import com.skyd.rays.ui.local.LocalNavController
import com.skyd.rays.ui.local.LocalStickerColorTheme
import com.skyd.rays.ui.local.LocalThemeName
import com.skyd.rays.ui.screen.settings.appearance.style.SEARCH_STYLE_SCREEN_ROUTE
import com.skyd.rays.ui.theme.extractColors
import com.skyd.rays.ui.theme.extractColorsFromWallpaper
import kotlinx.coroutines.launch

const val APPEARANCE_SCREEN_ROUTE = "appearanceScreen"

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
            RaysTopBar(
                style = RaysTopBarStyle.Large,
                scrollBehavior = scrollBehavior,
                title = { Text(text = stringResource(R.string.appearance_screen_name)) },
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
            item {
                BaseSettingsItem(
                    painter = rememberVectorPainter(image = Icons.Default.DarkMode),
                    text = stringResource(id = R.string.appearance_screen_dark_mode),
                    descriptionText = stringResource(id = R.string.appearance_screen_dark_mode_description),
                    onClick = { openDarkBottomSheet = true }
                )
            }
            item {
                SwitchSettingsItem(
                    imageVector = Icons.Default.Palette,
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
            item {
                CategorySettingsItem(text = stringResource(R.string.appearance_screen_style_category))
            }
            item {
                BaseSettingsItem(
                    painter = rememberVectorPainter(image = Icons.Default.Search),
                    text = stringResource(id = R.string.search_style_screen_name),
                    descriptionText = null,
                    onClick = { navController.navigate(SEARCH_STYLE_SCREEN_ROUTE) }
                )
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
        title = stringResource(R.string.primary_color),
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
    modifier: Modifier = Modifier,
    selected: Boolean,
    isCustom: Boolean = false,
    onClick: () -> Unit,
    accents: List<TonalPalette>,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = if (isCustom) {
            MaterialTheme.colorScheme.primaryContainer
                .copy(0.5f) onDark MaterialTheme.colorScheme.onPrimaryContainer.copy(0.3f)
        } else {
            MaterialTheme.colorScheme.inverseOnSurface
        },
    ) {
        Surface(
            modifier = Modifier
                .clickable { onClick() }
                .padding(12.dp)
                .size(50.dp),
            shape = CircleShape,
            color = Color(accents[0].tone(60)),
        ) {
            Box {
                Surface(
                    modifier = Modifier
                        .size(50.dp)
                        .offset((-25).dp, 25.dp),
                    color = Color(accents[1].tone(85)),
                ) {}
                Surface(
                    modifier = Modifier
                        .size(50.dp)
                        .offset(25.dp, 25.dp),
                    color = Color(accents[2].tone(75)),
                ) {}
                val animationSpec = spring<Float>(stiffness = Spring.StiffnessMedium)
                AnimatedVisibility(
                    visible = selected,
                    enter = scaleIn(animationSpec) + fadeIn(animationSpec),
                    exit = scaleOut(animationSpec) + fadeOut(animationSpec),
                ) {
                    Box(
                        modifier = Modifier
                            .padding(10.dp)
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Check,
                            contentDescription = "Checked",
                            modifier = Modifier
                                .padding(8.dp)
                                .size(16.dp),
                            tint = MaterialTheme.colorScheme.surface
                        )
                    }
                }
            }
        }
    }
}