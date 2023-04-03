package com.skyd.rays.ui.screen.settings.appearance

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.kyant.monet.TonalPalettes
import com.kyant.monet.TonalPalettes.Companion.toTonalPalettes
import com.skyd.rays.R
import com.skyd.rays.ext.checkColorHex
import com.skyd.rays.ext.onDark
import com.skyd.rays.ext.toColorOrNull
import com.skyd.rays.model.preference.theme.CustomPrimaryColorPreference
import com.skyd.rays.model.preference.theme.DarkModePreference
import com.skyd.rays.model.preference.theme.ThemeNamePreference
import com.skyd.rays.ui.component.*
import com.skyd.rays.ui.component.dialog.TextFieldDialog
import com.skyd.rays.ui.local.LocalCustomPrimaryColor
import com.skyd.rays.ui.local.LocalDarkMode
import com.skyd.rays.ui.local.LocalThemeName
import com.skyd.rays.ui.theme.extractTonalPalettes
import com.skyd.rays.ui.theme.extractTonalPalettesFromWallpaper

const val APPEARANCE_SCREEN_ROUTE = "appearanceScreen"

@Composable
fun AppearanceScreen() {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val themeName = LocalThemeName.current

    val tonalPalettes = extractTonalPalettes()
    val tonalPalettesFromWallpaper = extractTonalPalettesFromWallpaper()
    var wallpaperOrBasicThemeSelected by remember {
        mutableStateOf(if (tonalPalettesFromWallpaper.containsKey(themeName)) 0 else 1)
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
                .nestedScroll(scrollBehavior.nestedScrollConnection), contentPadding = paddingValues
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
                            Palettes(palettes = tonalPalettesFromWallpaper)
                        },
                        BlockRadioGroupButtonItem(
                            text = stringResource(R.string.appearance_screen_basic_colors),
                            onClick = {},
                        ) {
                            Palettes(palettes = tonalPalettes)
                        },
                    ),
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            item {
                BaseSettingsItem(
                    icon = rememberVectorPainter(image = Icons.Default.DarkMode),
                    text = stringResource(id = R.string.appearance_screen_dark_mode),
                    descriptionText = stringResource(id = R.string.appearance_screen_dark_mode_description),
                    onClick = { openDarkBottomSheet = true }
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
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .selectable(
                                selected = (it == darkMode),
                                onClick = {
                                    DarkModePreference.put(
                                        context = context, scope = scope,
                                        value = it
                                    )
                                },
                                role = Role.RadioButton
                            )
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (it == darkMode),
                            onClick = null // null recommended for accessibility with screen readers
                        )
                        Text(
                            text = DarkModePreference.toDisplayName(it),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun Palettes(
    palettes: Map<String, TonalPalettes>,
    themeName: String = LocalThemeName.current,
) {
    val customPrimaryColor = LocalCustomPrimaryColor.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val tonalPalettes = (customPrimaryColor.toColorOrNull() ?: Color.Transparent).toTonalPalettes()
    var addDialogVisible by remember { mutableStateOf(false) }
    var customColorValue by remember { mutableStateOf(customPrimaryColor) }

    if (palettes.isEmpty()) {
        Row(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .fillMaxWidth()
                .height(80.dp)
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
            palettes.forEach { (t, u) ->
                val isCustom = t == ThemeNamePreference.CUSTOM_THEME_NAME
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
                    palette = if (isCustom) tonalPalettes else u
                )
            }
        }
    }

    TextFieldDialog(
        visible = addDialogVisible,
        title = stringResource(R.string.primary_color),
        value = customColorValue,
        onValueChange = {
            customColorValue = it
        },
        onDismissRequest = {
            addDialogVisible = false
        },
        onConfirm = {
            it.checkColorHex()?.let { color ->
                CustomPrimaryColorPreference.put(context, scope, color)
                ThemeNamePreference.put(context, scope, ThemeNamePreference.CUSTOM_THEME_NAME)
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
    palette: TonalPalettes,
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
                .size(48.dp),
            shape = CircleShape,
            color = palette accent1 90.0,
        ) {
            Box {
                Surface(
                    modifier = Modifier
                        .size(48.dp)
                        .offset((-24).dp, 24.dp),
                    color = palette accent3 90.0,
                ) {}
                Surface(
                    modifier = Modifier
                        .size(48.dp)
                        .offset(24.dp, 24.dp),
                    color = palette accent2 60.0,
                ) {}
                AnimatedVisibility(visible = selected) {
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