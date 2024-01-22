package com.skyd.rays.ui.screen.minitool.styletransfer

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skyd.rays.R
import com.skyd.rays.base.mvi.getDispatcher
import com.skyd.rays.ext.isCompact
import com.skyd.rays.ext.plus
import com.skyd.rays.ext.showSnackbar
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

const val STYLE_TRANSFER_SCREEN_ROUTE = "styleTransferScreen"

@Composable
fun StyleTransferScreen(viewModel: StyleTransferViewModel = hiltViewModel()) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val uiState by viewModel.viewState.collectAsStateWithLifecycle()
    var styleUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var contentUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    val pickStyleLauncher = rememberImagePicker(multiple = false) {
        if (it.firstOrNull() != null) styleUri = it.first()
    }
    val pickContentLauncher = rememberImagePicker(multiple = false) {
        if (it.firstOrNull() != null) contentUri = it.first()
    }
    val lazyListState = rememberLazyListState()
    var fabHeight by remember { mutableStateOf(0.dp) }

    val dispatch = viewModel.getDispatcher(startWith = StyleTransferIntent.Initial)

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            RaysExtendedFloatingActionButton(
                text = { Text(text = stringResource(R.string.style_transfer_screen_transfer)) },
                icon = { Icon(imageVector = Icons.Default.Transform, contentDescription = null) },
                onClick = {
                    val style = styleUri
                    val content = contentUri
                    if (style == null || content == null) {
                        snackbarHostState.showSnackbar(
                            scope = scope,
                            message = context.getString(R.string.style_transfer_screen_image_not_selected)
                        )
                        return@RaysExtendedFloatingActionButton
                    }
                    dispatch(StyleTransferIntent.Transfer(style = style, content = content))
                },
                onSizeWithSinglePaddingChanged = { _, height -> fabHeight = height },
                contentDescription = stringResource(R.string.style_transfer_screen_transfer)
            )
        },
        topBar = {
            RaysTopBar(title = { Text(text = stringResource(id = R.string.style_transfer_screen_name)) })
        }
    ) { paddingValues ->
        val isCompact = LocalWindowSizeClass.current.isCompact
        val content: @Composable LazyItemScope.() -> Unit = remember {
            {
                InputArea(
                    modifier = Modifier.fillMaxWidth(if (isCompact) 1f else 0.5f),
                    styleUri = styleUri,
                    contentUri = contentUri,
                    onSelectStyleImage = { pickStyleLauncher.launchImagePicker() },
                    onSelectContentImage = { pickContentLauncher.launchImagePicker() },
                )
                val styleTransferResultState = uiState.styleTransferResultState
                if (styleTransferResultState is StyleTransferResultState.Success) {
                    ResultArea(bitmap = styleTransferResultState.image)
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

        WaitingDialog(visible = uiState.loadingDialog)
    }
}

@Composable
private fun ResultArea(bitmap: Bitmap) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        onClick = { context.sendSticker(bitmap = bitmap) }
    ) {
        RaysImage(
            model = bitmap,
            modifier = Modifier.fillMaxWidth(),
            blur = false,
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
        )
    }
}

@Composable
private fun InputArea(
    modifier: Modifier = Modifier,
    styleUri: Uri?,
    contentUri: Uri?,
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
                title = stringResource(R.string.style_transfer_screen_style),
                hintText = stringResource(R.string.style_transfer_screen_select_style_image),
                shape = CurlyCornerShape(amp = 5f, count = 12),
                imageUri = styleUri,
                onSelectImage = onSelectStyleImage,
            )
            Icon(
                modifier = Modifier.padding(10.dp),
                imageVector = Icons.Default.Add,
                contentDescription = null
            )
            InputItem(
                modifier = Modifier.weight(0.5f),
                title = stringResource(R.string.style_transfer_screen_content),
                hintText = stringResource(R.string.style_transfer_screen_select_content_image),
                shape = CloverShape,
                imageUri = contentUri,
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
            Column {
                RaysImage(
                    model = imageUri,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp),
                    blur = false,
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
