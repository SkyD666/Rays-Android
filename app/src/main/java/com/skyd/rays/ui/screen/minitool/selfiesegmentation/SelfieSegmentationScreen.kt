package com.skyd.rays.ui.screen.minitool.selfiesegmentation

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ContentCut
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skyd.rays.R
import com.skyd.rays.base.mvi.MviEventListener
import com.skyd.rays.base.mvi.getDispatcher
import com.skyd.rays.ext.isCompact
import com.skyd.rays.ext.plus
import com.skyd.rays.ext.showSnackbar
import com.skyd.rays.ui.component.ImageInput
import com.skyd.rays.ui.component.RaysExtendedFloatingActionButton
import com.skyd.rays.ui.component.RaysImage
import com.skyd.rays.ui.component.RaysTopBar
import com.skyd.rays.ui.component.dialog.WaitingDialog
import com.skyd.rays.ui.component.shape.CloverShape
import com.skyd.rays.ui.component.shape.CurlyCornerShape
import com.skyd.rays.ui.local.LocalWindowSizeClass
import com.skyd.rays.util.launchImagePicker
import com.skyd.rays.util.rememberImagePicker
import com.skyd.rays.util.sendSticker
import kotlinx.serialization.Serializable


@Serializable
data object SelfieSegmentationRoute

@Composable
fun SelfieSegmentationScreen(viewModel: SelfieSegmentationViewModel = hiltViewModel()) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val uiState by viewModel.viewState.collectAsStateWithLifecycle()
    var selfieUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    val pickSelfieLauncher = rememberImagePicker(multiple = false) {
        if (it.firstOrNull() != null) selfieUri = it.first()
    }
    var backgroundUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    val pickBackgroundLauncher = rememberImagePicker(multiple = false) {
        if (it.firstOrNull() != null) backgroundUri = it.first()
    }
    val lazyListState = rememberLazyListState()
    var fabHeight by remember { mutableStateOf(0.dp) }

    val dispatch = viewModel.getDispatcher(startWith = SelfieSegmentationIntent.Initial)

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            RaysExtendedFloatingActionButton(
                text = { Text(text = stringResource(R.string.selfie_segmentation_screen_segment)) },
                icon = { Icon(imageVector = Icons.Outlined.ContentCut, contentDescription = null) },
                onClick = {
                    val foreground = selfieUri
                    if (foreground == null) {
                        snackbarHostState.showSnackbar(
                            scope = scope,
                            message = context.getString(R.string.selfie_segmentation_screen_image_not_selected)
                        )
                        return@RaysExtendedFloatingActionButton
                    }
                    dispatch(SelfieSegmentationIntent.Segment(foregroundUri = foreground))
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
                    onSelectSelfieImage = { pickSelfieLauncher.launchImagePicker() },
                    onSelectBackgroundImage = { pickBackgroundLauncher.launchImagePicker() },
                    onRemoveBackgroundImage = { backgroundUri = null }
                )
                val selfieSegmentationResultState = uiState.selfieSegmentationResultState
                if (selfieSegmentationResultState is SelfieSegmentationResultState.Success) {
                    ResultArea(
                        bitmap = selfieSegmentationResultState.image,
                        backgroundUri = backgroundUri,
                        onExport = { scale, offset, rotation, foregroundSize, backgroundSize, borderSize ->
                            dispatch(
                                SelfieSegmentationIntent.Export(
                                    foregroundBitmap = selfieSegmentationResultState.image,
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

        MviEventListener(viewModel.singleEvent) { event ->
            when (event) {
                is SelfieSegmentationEvent.ExportUiEvent.Success -> context.sendSticker(bitmap = event.bitmap)
                is SelfieSegmentationEvent.SegmentUiEvent.Failed -> snackbarHostState.showSnackbar(
                    context.getString(R.string.failed_info, event.msg),
                )
            }
        }

        WaitingDialog(visible = uiState.loadingDialog)
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
                    blur = false,
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
                    blur = false,
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
            ImageInput(
                modifier = Modifier.weight(0.5f),
                title = stringResource(R.string.selfie_segmentation_screen_selfie),
                hintText = stringResource(R.string.selfie_segmentation_screen_select_selfie_image),
                shape = CurlyCornerShape(amp = 5f, count = 12),
                imageUri = selfieUri,
                onSelectImage = onSelectSelfieImage,
            )
            Icon(
                modifier = Modifier.padding(10.dp),
                imageVector = Icons.Outlined.Add,
                contentDescription = null
            )
            ImageInput(
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