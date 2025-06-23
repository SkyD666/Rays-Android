package com.skyd.rays.ui.screen.minitool.styletransfer

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Transform
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skyd.compone.component.ComponeExtendedFloatingActionButton
import com.skyd.compone.component.ComponeTopBar
import com.skyd.compone.component.dialog.WaitingDialog
import com.skyd.compone.ext.plus
import com.skyd.rays.R
import com.skyd.rays.base.mvi.getDispatcher
import com.skyd.rays.ext.isCompact
import com.skyd.rays.ext.showSnackbar
import com.skyd.rays.ui.component.ImageInput
import com.skyd.rays.ui.component.RaysImage
import com.skyd.rays.ui.local.LocalWindowSizeClass
import com.skyd.rays.util.launchImagePicker
import com.skyd.rays.util.rememberImagePicker
import com.skyd.rays.util.sendSticker
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel


@Serializable
data object StyleTransferRoute

@Composable
fun StyleTransferScreen(viewModel: StyleTransferViewModel = koinViewModel()) {
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
            ComponeExtendedFloatingActionButton(
                text = { Text(text = stringResource(R.string.style_transfer_screen_transfer)) },
                icon = { Icon(imageVector = Icons.Outlined.Transform, contentDescription = null) },
                onClick = {
                    val style = styleUri
                    val content = contentUri
                    if (style == null || content == null) {
                        snackbarHostState.showSnackbar(
                            scope = scope,
                            message = context.getString(R.string.style_transfer_screen_image_not_selected)
                        )
                        return@ComponeExtendedFloatingActionButton
                    }
                    dispatch(StyleTransferIntent.Transfer(style = style, content = content))
                },
                onSizeWithSinglePaddingChanged = { _, height -> fabHeight = height },
                contentDescription = stringResource(R.string.style_transfer_screen_transfer)
            )
        },
        topBar = {
            ComponeTopBar(title = { Text(text = stringResource(id = R.string.style_transfer_screen_name)) })
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
            ImageInput(
                modifier = Modifier.weight(0.5f),
                title = stringResource(R.string.style_transfer_screen_style),
                hintText = stringResource(R.string.style_transfer_screen_select_style_image),
                shape = MaterialShapes.Cookie12Sided.toShape(),
                imageUri = styleUri,
                contentScale = ContentScale.FillWidth,
                onSelectImage = onSelectStyleImage,
            )
            Icon(
                modifier = Modifier.padding(10.dp),
                imageVector = Icons.Outlined.Add,
                contentDescription = null
            )
            ImageInput(
                modifier = Modifier.weight(0.5f),
                title = stringResource(R.string.style_transfer_screen_content),
                hintText = stringResource(R.string.style_transfer_screen_select_content_image),
                shape = MaterialShapes.Clover4Leaf.toShape(),
                imageUri = contentUri,
                onSelectImage = onSelectContentImage,
            )
        }
    }
}