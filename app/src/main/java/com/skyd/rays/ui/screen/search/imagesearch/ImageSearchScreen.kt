package com.skyd.rays.ui.screen.search.imagesearch

import android.net.Uri
import android.os.Bundle
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
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.skyd.rays.R
import com.skyd.rays.base.mvi.MviEventListener
import com.skyd.rays.base.mvi.getDispatcher
import com.skyd.rays.ext.isCompact
import com.skyd.rays.ext.navigate
import com.skyd.rays.ext.plus
import com.skyd.rays.model.preference.search.imagesearch.ImageSearchMaxResultCountPreference
import com.skyd.rays.ui.component.ImageInput
import com.skyd.rays.ui.component.RaysExtendedFloatingActionButton
import com.skyd.rays.ui.component.RaysTopBar
import com.skyd.rays.ui.component.dialog.WaitingDialog
import com.skyd.rays.ui.component.shape.CurlyCornerShape
import com.skyd.rays.ui.local.LocalImageSearchMaxResultCount
import com.skyd.rays.ui.local.LocalWindowSizeClass
import com.skyd.rays.ui.screen.stickerslist.StickerList
import com.skyd.rays.util.launchImagePicker
import com.skyd.rays.util.rememberImagePicker

const val IMAGE_SEARCH_SCREEN_ROUTE = "imageSearchScreen"
const val BASE_IMAGE_KEY = "baseImage"

fun openImageSearchScreen(navController: NavHostController, baseImage: Uri?) {
    navController.navigate(
        IMAGE_SEARCH_SCREEN_ROUTE,
        Bundle().apply {
            putParcelable(BASE_IMAGE_KEY, baseImage)
        }
    )
}

@Composable
fun ImageSearchScreen(baseImage: Uri?, viewModel: ImageSearchViewModel = hiltViewModel()) {
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val uiState by viewModel.viewState.collectAsStateWithLifecycle()
    var fabHeight by remember { mutableStateOf(0.dp) }
    var currentBaseImage by rememberSaveable(baseImage) { mutableStateOf(baseImage) }
    val imagePickLauncher = rememberImagePicker(multiple = false) {
        if (it.firstOrNull() != null) currentBaseImage = it.first()
    }

    val dispatch = viewModel.getDispatcher(startWith = ImageSearchIntent.Init)

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            val maxCount = LocalImageSearchMaxResultCount.current
            RaysExtendedFloatingActionButton(
                text = { Text(text = stringResource(R.string.image_search_screen_search)) },
                icon = { Icon(imageVector = Icons.Outlined.Search, contentDescription = null) },
                onClick = {
                    if (currentBaseImage != null) {
                        dispatch(ImageSearchIntent.Search(currentBaseImage!!, maxCount))
                    }
                },
                onSizeWithSinglePaddingChanged = { _, height -> fabHeight = height },
                contentDescription = stringResource(R.string.image_search_screen_search)
            )
        },
        topBar = {
            RaysTopBar(title = { Text(text = stringResource(id = R.string.image_search_screen_name)) })
        }
    ) { paddingValues ->
        val isCompact = LocalWindowSizeClass.current.isCompact
        val inputItem: @Composable () -> Unit = remember {
            {
                ImageInput(
                    modifier = Modifier.sizeIn(maxWidth = 120.dp, maxHeight = 170.dp),
                    title = stringResource(R.string.image_search_screen_source_image),
                    hintText = stringResource(R.string.image_search_screen_source_image_hint),
                    shape = CurlyCornerShape(amp = 5f, count = 12),
                    imageUri = currentBaseImage,
                    onSelectImage = { imagePickLauncher.launchImagePicker() },
                )
            }
        }
        val stickerList: @Composable () -> Unit = remember {
            {
                val imageSearchResultState = uiState.imageSearchResultState
                if (imageSearchResultState is ImageSearchResultState.Success) {
                    StickerList(
                        count = imageSearchResultState.stickers.size,
                        onData = { imageSearchResultState.stickers[it] },
                        key = { imageSearchResultState.stickers[it].sticker.uuid },
                        contentPadding = PaddingValues(horizontal = 16.dp) +
                                PaddingValues(bottom = fabHeight + 16.dp),
                    )
                }
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
                stickerList()
            }
        } else {
            Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                        .weight(0.25f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    inputItem()
                    Spacer(modifier = Modifier.height(12.dp))
                    MaxResultCountSlider()
                }
                Box(modifier = Modifier.weight(0.75f)) {
                    stickerList()
                }
            }
        }

        MviEventListener(viewModel.singleEvent) { event ->
            when (event) {
                is ImageSearchEvent.SearchUiEvent.Failed -> snackbarHostState.showSnackbar(
                    context.getString(R.string.failed_info, event.msg),
                )
            }
        }

        WaitingDialog(
            visible = uiState.loadingDialog,
            text = { Text(stringResource(R.string.image_search_screen_long_time_tip)) },
        )
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