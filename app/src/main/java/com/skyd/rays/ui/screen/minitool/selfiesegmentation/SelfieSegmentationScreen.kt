package com.skyd.rays.ui.screen.minitool.selfiesegmentation

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skyd.rays.R
import com.skyd.rays.base.LoadUiIntent
import com.skyd.rays.ext.isCompact
import com.skyd.rays.ext.plus
import com.skyd.rays.ext.showSnackbar
import com.skyd.rays.ui.component.RaysExtendedFloatingActionButton
import com.skyd.rays.ui.component.RaysIconButton
import com.skyd.rays.ui.component.RaysIconButtonStyle
import com.skyd.rays.ui.component.RaysImage
import com.skyd.rays.ui.component.RaysTopBar
import com.skyd.rays.ui.component.dialog.WaitingDialog
import com.skyd.rays.ui.component.shape.CloverShape
import com.skyd.rays.ui.component.shape.CurlyCornerShape
import com.skyd.rays.ui.local.LocalWindowSizeClass
import com.skyd.rays.util.sendSticker

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
    var fabHeight by remember { mutableStateOf(0.dp) }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            RaysExtendedFloatingActionButton(
                text = { Text(text = stringResource(R.string.selfie_segmentation_screen_segment)) },
                icon = { Icon(imageVector = Icons.Default.ContentCut, contentDescription = null) },
                onClick = {
                    val foreground = selfieUri
                    if (foreground == null) {
                        snackbarHostState.showSnackbar(
                            scope = scope,
                            message = context.getString(R.string.selfie_segmentation_screen_image_not_selected)
                        )
                        return@RaysExtendedFloatingActionButton
                    }
                    viewModel.sendUiIntent(
                        SelfieSegmentationIntent.Segment(foregroundUri = foreground)
                    )
                },
                onSizeWithSinglePaddingChanged = { _, height -> fabHeight = height },
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
                    onSelectSelfieImage = { pickSelfieLauncher.launch("image/*") },
                    onSelectBackgroundImage = { pickBackgroundLauncher.launch("image/*") },
                    onRemoveBackgroundImage = { backgroundUri = null }
                )
                val selfieSegmentationResultUiState = uiState.selfieSegmentationResultUiState
                if (selfieSegmentationResultUiState is SelfieSegmentationResultUiState.Success) {
                    ResultArea(
                        bitmap = selfieSegmentationResultUiState.image,
                        backgroundUri = backgroundUri,
                        onExport = { scale, offset, rotation, foregroundSize, backgroundSize, borderSize ->
                            viewModel.sendUiIntent(
                                SelfieSegmentationIntent.Export(
                                    foregroundBitmap = selfieSegmentationResultUiState.image,
                                    backgroundUri = backgroundUri,
                                    backgroundSize = backgroundSize,
                                    foregroundScale = scale,
                                    foregroundOffset = offset,
                                    foregroundRotation = rotation,
                                    foregroundSize = foregroundSize,
                                    borderSize = borderSize,
                                )
                            )
                        },
                    )
                }
            }
        }
        LazyColumn(
            state = lazyListState,
            contentPadding = paddingValues + PaddingValues(bottom = fabHeight),
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
private fun ResetArea(onResetSelfie: () -> Unit) {
    Row(modifier = Modifier.padding(horizontal = 16.dp)) {
        Button(modifier = Modifier.weight(1f), onClick = onResetSelfie) {
            Text(text = stringResource(R.string.selfie_segmentation_screen_reset_selfie_transformations))
        }
    }
}

@Composable
private fun ResultArea(
    bitmap: Bitmap,
    backgroundUri: Uri?,
    onExport: (
        scale: Float,
        offset: Offset,
        rotation: Float,
        foregroundSize: IntSize,
        backgroundSize: IntSize,
        borderSize: IntSize,
    ) -> Unit
) {
    var borderSize by remember { mutableStateOf(IntSize.Zero) }
    var backgroundSize by remember { mutableStateOf(IntSize.Zero) }

    // drag Selfie的位置
    var foregroundSize by remember { mutableStateOf(IntSize.Zero) }
    var scale by remember { mutableFloatStateOf(1f) }
    var rotation by remember { mutableFloatStateOf(0f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var matrix by remember { mutableStateOf(Matrix()) }

    var useAnimate by remember { mutableStateOf(false) }
    val animateFinishedListener: (Any) -> Unit = { useAnimate = false }

    Column {
        ResetArea(onResetSelfie = {
            useAnimate = true
            scale = 1f
            rotation = 0f
            offset = Offset.Zero
            matrix.reset()
        })

        Card(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .width(IntrinsicSize.Min),
            onClick = {
                onExport(
                    scale,
                    offset,
                    rotation,
                    foregroundSize,
                    backgroundSize,
                    borderSize,
                )
            },
        ) {
            val scaleAnimate: Float by animateFloatAsState(
                targetValue = scale,
                label = "scaleAnimate",
                finishedListener = animateFinishedListener
            )
            val rotationAnimate: Float by animateFloatAsState(
                targetValue = rotation,
                label = "rotationAnimate",
                finishedListener = animateFinishedListener
            )
            val offsetAnimate: Offset by animateOffsetAsState(
                targetValue = offset,
                label = "offsetAnimate",
                finishedListener = animateFinishedListener
            )

            Box(modifier = Modifier.onSizeChanged { borderSize = it }) {
                RaysImage(
                    model = backgroundUri,
                    modifier = Modifier
                        .onSizeChanged { backgroundSize = it }
                        .fillMaxWidth(),
                    contentDescription = null,
                    contentScale = ContentScale.FillWidth,
                )
                RaysImage(
                    model = bitmap,
                    modifier = Modifier
                        .graphicsLayer {
                            if (useAnimate) {
                                translationX = offsetAnimate.x
                                translationY = offsetAnimate.y
                                scaleX = scaleAnimate
                                scaleY = scaleAnimate
                                rotationZ = rotationAnimate
                            } else {
                                translationX = offset.x
                                translationY = offset.y
                                scaleX = scale
                                scaleY = scale
                                rotationZ = rotation
                            }
                        }
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, r ->
                                rotation += r
                                scale *= zoom

                                // ------------- 下面是矫正正确的偏移Offset
                                // 对矩阵先这样，再那样那样，现在不用担心顺序了，矩阵的rotateZ()是原地打转的
                                // 想让它围着某点打转可以用 rotationMatrix(degrees , px , py)创建一个这样的矩阵
                                // 这三者必须都用上，最终才能得到贴合手指移动的正确结果
                                matrix.translate(pan.x, pan.y)
                                matrix.rotateZ(r)
                                matrix.scale(zoom, zoom)

                                // 把修改后的matrix保存起来以继承状态啊
                                matrix = Matrix(matrix.values)

                                // 新的offset就可以通过矩阵计算得到正确的值了
                                offset = Offset(
                                    matrix.values[Matrix.TranslateX],
                                    matrix.values[Matrix.TranslateY]
                                )
                            }
                        }
                        .onSizeChanged { foregroundSize = it },
                    contentDescription = null,
                    contentScale = ContentScale.Inside,
                )
            }
        }
    }
}

@Composable
private fun InputArea(
    modifier: Modifier = Modifier,
    selfieUri: Uri?,
    backgroundUri: Uri?,
    onSelectSelfieImage: () -> Unit,
    onSelectBackgroundImage: () -> Unit,
    onRemoveBackgroundImage: () -> Unit,
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
                onSelectImage = onSelectSelfieImage,
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
                onRemoveClick = onRemoveBackgroundImage,
                onSelectImage = onSelectBackgroundImage,
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
    onRemoveClick: (() -> Unit)? = null,
    onSelectImage: () -> Unit
) {
    OutlinedCard(modifier = modifier, onClick = onSelectImage) {
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
            Box {
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
                if (onRemoveClick != null) {
                    RaysIconButton(
                        modifier = Modifier.align(Alignment.TopEnd),
                        onClick = onRemoveClick,
                        imageVector = Icons.Default.Close,
                        style = RaysIconButtonStyle.Filled,
                    )
                }
            }
        }
    }
}
