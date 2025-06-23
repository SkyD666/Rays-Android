package com.skyd.rays.ui.screen.search.imagesearch

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Deselect
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SelectAll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skyd.compone.component.ComponeFloatingActionButton
import com.skyd.compone.component.ComponeIconToggleButton
import com.skyd.compone.component.ComponeTopBar
import com.skyd.compone.component.dialog.WaitingDialog
import com.skyd.compone.ext.plus
import com.skyd.compone.local.LocalNavController
import com.skyd.rays.R
import com.skyd.rays.base.mvi.MviEventListener
import com.skyd.rays.base.mvi.getDispatcher
import com.skyd.rays.ext.isCompact
import com.skyd.rays.model.preference.search.imagesearch.ImageSearchMaxResultCountPreference
import com.skyd.rays.ui.component.ImageInput
import com.skyd.rays.ui.local.LocalImageSearchMaxResultCount
import com.skyd.rays.ui.local.LocalWindowSizeClass
import com.skyd.rays.ui.screen.detail.DetailRoute
import com.skyd.rays.ui.screen.fullimage.WrappedUriNullable
import com.skyd.rays.ui.screen.search.multiselect.MultiSelectActionBar
import com.skyd.rays.ui.screen.stickerslist.StickerList
import com.skyd.rays.util.launchImagePicker
import com.skyd.rays.util.rememberImagePicker
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel


@Serializable
data class ImageSearchRoute(val uri: WrappedUriNullable) {
    constructor(baseImage: Uri?) : this(WrappedUriNullable(image = baseImage))
}

@Composable
fun ImageSearchScreen(baseImage: Uri?, viewModel: ImageSearchViewModel = koinViewModel()) {
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val navController = LocalNavController.current
    val uiState by viewModel.viewState.collectAsStateWithLifecycle()
    var fabHeight by remember { mutableStateOf(0.dp) }
    var fabWidth by remember { mutableStateOf(0.dp) }
    var currentBaseImage by rememberSaveable(baseImage) { mutableStateOf(baseImage) }
    val imagePickLauncher = rememberImagePicker(multiple = false) {
        if (it.firstOrNull() != null) currentBaseImage = it.first()
    }
    var multiSelect by rememberSaveable { mutableStateOf(false) }

    val dispatch = viewModel.getDispatcher(startWith = ImageSearchIntent.Init)

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            val maxCount = LocalImageSearchMaxResultCount.current
            ComponeFloatingActionButton(
                onClick = {
                    if (currentBaseImage != null) {
                        dispatch(ImageSearchIntent.Search(currentBaseImage!!, maxCount))
                    }
                },
                onSizeWithSinglePaddingChanged = { width, height ->
                    fabWidth = width
                    fabHeight = height
                },
                contentDescription = stringResource(R.string.image_search_screen_search)
            ) {
                Icon(imageVector = Icons.Outlined.Search, contentDescription = null)
            }
        },
        topBar = {
            ComponeTopBar(
                title = { Text(text = stringResource(id = R.string.image_search_screen_name)) },
                actions = {
                    ComponeIconToggleButton(
                        checked = multiSelect,
                        onCheckedChange = {
                            multiSelect = it
                            if (!it) {
                                dispatch(ImageSearchIntent.RemoveSelectedStickers(uiState.selectedStickers))
                            }
                        },
                        imageVector = if (multiSelect) Icons.Outlined.SelectAll else Icons.Outlined.Deselect,
                    )
                }
            )
        }
    ) { paddingValues ->
        val isCompact = LocalWindowSizeClass.current.isCompact
        val inputItem: @Composable () -> Unit = remember {
            {
                ImageInput(
                    modifier = Modifier.sizeIn(maxWidth = 120.dp),
                    title = stringResource(R.string.image_search_screen_source_image),
                    hintText = stringResource(R.string.image_search_screen_source_image_hint),
                    shape = MaterialShapes.Cookie12Sided.toShape(),
                    imageUri = currentBaseImage,
                    maxImageHeight = 150.dp,
                    onSelectImage = { imagePickLauncher.launchImagePicker() },
                )
            }
        }
        val stickerList: @Composable () -> Unit = {
            val imageSearchResultState = uiState.imageSearchResultState
            if (imageSearchResultState is ImageSearchResultState.Success) {
                StickerList(
                    count = imageSearchResultState.stickers.size,
                    onData = { imageSearchResultState.stickers[it] },
                    key = { imageSearchResultState.stickers[it].sticker.uuid },
                    selectable = multiSelect,
                    selected = { it.sticker.uuid in uiState.selectedStickers },
                    contentPadding = PaddingValues(horizontal = 16.dp) +
                            PaddingValues(bottom = fabHeight + 16.dp),
                    onSelectChanged = { data, selected ->
                        if (selected) {
                            dispatch(ImageSearchIntent.AddSelectedStickers(listOf(data.sticker.uuid)))
                        } else {
                            dispatch(ImageSearchIntent.RemoveSelectedStickers(listOf(data.sticker.uuid)))
                        }
                    },
                    onClick = { navController.navigate(DetailRoute(stickerUuid = it.sticker.uuid)) },
                )
            }
        }

        val multiSelectBar: @Composable (compact: Boolean) -> Unit = { compact ->
            AnimatedVisibility(
                visible = multiSelect,
                enter = if (compact) expandVertically() else expandHorizontally(),
                exit = if (compact) shrinkVertically() else shrinkHorizontally(),
            ) {
                MultiSelectActionBar(
                    modifier = Modifier.run {
                        if (isCompact) padding(end = fabWidth)
                        else this
                    },
                    selectedStickers = uiState.selectedStickers,
                    onRemoveSelectedStickers = {
                        dispatch(ImageSearchIntent.RemoveSelectedStickers(it))
                    }
                )
            }
        }
        val modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
        if (isCompact) {
            Column(
                modifier = modifier,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    inputItem()
                    Spacer(modifier = Modifier.width(12.dp))
                    MaxResultCountSlider()
                }
                Box(modifier = Modifier.weight(1f)) { stickerList() }
                multiSelectBar(true)
            }
        } else {
            Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                        .width(200.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    inputItem()
                    Spacer(modifier = Modifier.height(12.dp))
                    MaxResultCountSlider()
                }
                multiSelectBar(false)
                Box(modifier = Modifier.weight(1f)) { stickerList() }
            }
        }

        WaitingDialog(
            visible = uiState.loadingDialog,
            text = { Text(stringResource(R.string.image_search_screen_long_time_tip)) },
        )
    }

    MviEventListener(viewModel.singleEvent) { event ->
        when (event) {
            is ImageSearchEvent.UpdateImageUiEvent.Failed -> snackbarHostState.showSnackbar(
                context.getString(R.string.failed_info, event.msg),
            )
        }
    }
}

@Composable
private fun MaxResultCountSlider() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val maxCount = LocalImageSearchMaxResultCount.current
    var maxCountFloat by rememberSaveable { mutableFloatStateOf(maxCount.toFloat()) }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = stringResource(
                R.string.image_search_screen_max_result_count,
                maxCountFloat.toInt()
            ),
            style = MaterialTheme.typography.labelLarge,
        )
        Slider(
            maxCountFloat,
            onValueChange = {
                ImageSearchMaxResultCountPreference.put(
                    context = context, scope = scope, value = it.toInt()
                )
                maxCountFloat = it
            },
            valueRange = 1f..100f,
        )
    }
}