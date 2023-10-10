package com.skyd.rays.ui.screen.minitool.selfiesegmentation

import android.graphics.Bitmap
import android.graphics.RectF
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skyd.rays.R
import com.skyd.rays.base.LoadUiIntent
import com.skyd.rays.ext.inBottomOrNotLarge
import com.skyd.rays.ext.isCompact
import com.skyd.rays.ext.showSnackbar
import com.skyd.rays.ui.component.BottomHideExtendedFloatingActionButton
import com.skyd.rays.ui.component.RaysImage
import com.skyd.rays.ui.component.RaysOutlinedCard
import com.skyd.rays.ui.component.RaysTopBar
import com.skyd.rays.ui.component.dialog.WaitingDialog
import com.skyd.rays.ui.component.shape.CloverShape
import com.skyd.rays.ui.component.shape.CurlyCornerShape
import com.skyd.rays.ui.local.LocalWindowSizeClass
import com.skyd.rays.util.sendSticker
import kotlin.math.roundToInt

const val SELFIE_SEGMENTATION_SCREEN_ROUTE = "selfieSegmentationScreen"

@Composable
fun SelfieSegmentationScreen(viewModel: SelfieSegmentationViewModel = hiltViewModel()) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var openWaitingDialog by remember { mutableStateOf(false) }
    val uiState by viewModel.uiStateFlow.collectAsStateWithLifecycle()
    val uiEvent by viewModel.uiEventFlow.collectAsStateWithLifecycle(initialValue = null)
    val loadUiIntentFlow by viewModel.loadUiIntentFlow.collectAsStateWithLifecycle(initialValue = null)
    var selfieUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    val pickSelfieLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { if (it != null) selfieUri = it }
    var backgroundUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    val pickBackgroundLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { if (it != null) backgroundUri = it }
    val lazyListState = rememberLazyListState()
    val fabVisibility by remember {
        derivedStateOf { lazyListState.inBottomOrNotLarge }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            BottomHideExtendedFloatingActionButton(
                visible = fabVisibility,
                text = { Text(text = stringResource(R.string.selfie_segmentation_screen_segment)) },
                icon = { Icon(imageVector = Icons.Default.ContentCut, contentDescription = null) },
                onClick = {
                    val foreground = selfieUri
                    if (foreground == null) {
                        snackbarHostState.showSnackbar(
                            scope = scope,
                            message = context.getString(R.string.selfie_segmentation_screen_image_not_selected)
                        )
                        return@BottomHideExtendedFloatingActionButton
                    }
                    viewModel.sendUiIntent(
                        SelfieSegmentationIntent.Segment(foregroundUri = foreground)
                    )
                },
                contentDescription = stringResource(R.string.selfie_segmentation_screen_segment)
            )
        },
        topBar = {
            RaysTopBar(title = { Text(text = stringResource(id = R.string.selfie_segmentation_screen_name)) })
        }
    ) { paddingValues ->
        val isCompact = LocalWindowSizeClass.current.isCompact
        val content: @Composable LazyItemScope.() -> Unit = remember {
            {
                InputArea(
                    modifier = Modifier.fillMaxWidth(if (isCompact) 1f else 0.5f),
                    selfieUri = selfieUri,
                    backgroundUri = backgroundUri,
                    onSelectStyleImage = { pickSelfieLauncher.launch("image/*") },
                    onSelectContentImage = { pickBackgroundLauncher.launch("image/*") },
                )
                val selfieSegmentationResultUiState = uiState.selfieSegmentationResultUiState
                if (selfieSegmentationResultUiState is SelfieSegmentationResultUiState.Success) {
                    ResultArea(
                        bitmap = selfieSegmentationResultUiState.image,
                        backgroundUri = backgroundUri,
                        onExport = {
                            viewModel.sendUiIntent(
                                SelfieSegmentationIntent.Export(
                                    foregroundBitmap = selfieSegmentationResultUiState.image,
                                    backgroundUri = backgroundUri,
                                    foregroundRect = it,
                                )
                            )
                        },
                    )
                }
            }
        }
        LazyColumn(
            state = lazyListState,
            contentPadding = paddingValues,
        ) {
            item {
                if (isCompact) {
                    content()
                } else {
                    Row { content() }
                }
            }
        }

        when (val exportUiEvent = uiEvent?.exportUiEvent) {
            is ExportUiEvent.Success -> {
                LaunchedEffect(uiEvent) {
                    context.sendSticker(bitmap = exportUiEvent.bitmap)
                }
            }

            ExportUiEvent.Init,
            null -> Unit
        }

        loadUiIntentFlow?.also { loadUiIntent ->
            when (loadUiIntent) {
                is LoadUiIntent.Error -> {}
                is LoadUiIntent.Loading -> {
                    openWaitingDialog = loadUiIntent.isShow
                }
            }
        }
        WaitingDialog(visible = openWaitingDialog)
    }
}

@Composable
private fun ResultArea(bitmap: Bitmap, backgroundUri: Uri?, onExport: (RectF) -> Unit) {
    var componentRect by remember { mutableStateOf(RectF()) }
    var parentSize by remember { mutableStateOf(IntSize.Zero) }

    // drag Selfie的位置
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    Card(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
//            .height(IntrinsicSize.Min)
            .width(IntrinsicSize.Min),
        onClick = {
            componentRect.set(
                (componentRect.left / parentSize.width),
                (componentRect.top / parentSize.height),
                (componentRect.right / parentSize.width),
                (componentRect.bottom / parentSize.height)
            )
            onExport(componentRect)
        },
    ) {
        Box {
            RaysImage(
                model = backgroundUri,
                modifier = Modifier
                    .onSizeChanged { parentSize = it }
                    .fillMaxWidth(),
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
            )
            RaysImage(
                model = bitmap,
                modifier = Modifier
                    .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            offsetX += dragAmount.x
                            offsetY += dragAmount.y
                        }
                    }
                    .onGloballyPositioned {
                        componentRect = run {
                            val boundsInParent = it.boundsInParent()
                            RectF(
                                boundsInParent.left,
                                boundsInParent.top,
                                boundsInParent.right,
                                boundsInParent.bottom,
                            )
                        }
                    },
                contentDescription = null,
                contentScale = ContentScale.Inside,
            )
        }
    }
}

@Composable
private fun InputArea(
    modifier: Modifier = Modifier,
    selfieUri: Uri?,
    backgroundUri: Uri?,
    onSelectStyleImage: () -> Unit,
    onSelectContentImage: () -> Unit,
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.End,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            InputItem(
                modifier = Modifier.weight(0.5f),
                title = stringResource(R.string.selfie_segmentation_screen_selfie),
                hintText = stringResource(R.string.selfie_segmentation_screen_select_selfie_image),
                shape = CurlyCornerShape(amp = 5f, count = 12),
                imageUri = selfieUri,
                onSelectImage = onSelectStyleImage,
            )
            Icon(
                modifier = Modifier.padding(10.dp),
                imageVector = Icons.Default.Add,
                contentDescription = null
            )
            InputItem(
                modifier = Modifier.weight(0.5f),
                title = stringResource(R.string.selfie_segmentation_screen_background),
                hintText = stringResource(R.string.selfie_segmentation_screen_select_background_image),
                shape = CloverShape,
                imageUri = backgroundUri,
                onSelectImage = onSelectContentImage,
            )
        }
    }
}

@Composable
private fun InputItem(
    modifier: Modifier = Modifier,
    title: String,
    hintText: String,
    shape: Shape,
    imageUri: Uri?,
    onSelectImage: () -> Unit
) {
    RaysOutlinedCard(modifier = modifier, onClick = onSelectImage) {
        AnimatedVisibility(
            visible = imageUri == null,
            modifier = Modifier.clickable(onClick = onSelectImage)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .padding(6.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = shape,
                        )
                        .padding(16.dp)
                ) {
                    Icon(
                        modifier = Modifier.size(40.dp),
                        imageVector = Icons.Default.Image,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    modifier = Modifier.padding(horizontal = 6.dp),
                    text = hintText,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )
            }
        }
        AnimatedVisibility(
            visible = imageUri != null,
            modifier = Modifier.clickable(onClick = onSelectImage)
        ) {
            Column {
                RaysImage(
                    model = imageUri,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                )
                Text(
                    modifier = Modifier
                        .padding(6.dp)
                        .align(Alignment.CenterHorizontally),
                    text = title,
                )
            }
        }
    }
}
