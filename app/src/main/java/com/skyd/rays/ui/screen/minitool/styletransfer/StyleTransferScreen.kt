package com.skyd.rays.ui.screen.minitool.styletransfer

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skyd.rays.R
import com.skyd.rays.base.LoadUiIntent
import com.skyd.rays.ext.showSnackbar
import com.skyd.rays.ui.component.RaysCard
import com.skyd.rays.ui.component.RaysExtendedFloatingActionButton
import com.skyd.rays.ui.component.RaysImage
import com.skyd.rays.ui.component.RaysTopBar
import com.skyd.rays.ui.component.dialog.WaitingDialog
import com.skyd.rays.ui.local.LocalWindowSizeClass
import com.skyd.rays.util.sendSticker

const val STYLE_TRANSFER_SCREEN_ROUTE = "styleTransferScreen"

@Composable
fun StyleTransferScreen(viewModel: StyleTransferViewModel = hiltViewModel()) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var openWaitingDialog by remember { mutableStateOf(false) }
    val uiState by viewModel.uiStateFlow.collectAsStateWithLifecycle()
    val loadUiIntentFlow by viewModel.loadUiIntentFlow.collectAsStateWithLifecycle(initialValue = null)
    var styleUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var contentUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    val pickStyleLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { if (it != null) styleUri = it }
    val pickContentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { if (it != null) contentUri = it }
    val columnScrollState = rememberScrollState()
    val fabVisibility by remember {
        derivedStateOf {
            columnScrollState.value < columnScrollState.maxValue || columnScrollState.maxValue == 0
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            TransferExtendedFloatingActionButton(visible = fabVisibility) {
                val style = styleUri
                val content = contentUri
                if (style == null || content == null) {
                    snackbarHostState.showSnackbar(
                        scope = scope,
                        message = context.getString(R.string.style_transfer_screen_image_not_selected)
                    )
                    return@TransferExtendedFloatingActionButton
                }
                viewModel.sendUiIntent(
                    StyleTransferIntent.Transfer(
                        style = style,
                        content = content,
                    )
                )
            }
        },
        topBar = {
            RaysTopBar(
                title = { Text(text = stringResource(id = R.string.style_transfer_screen_name)) },
            )
        }
    ) { paddingValues ->
        val windowSizeClass = LocalWindowSizeClass.current
        if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact) {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .verticalScroll(columnScrollState)
            ) {
                InputArea(
                    styleUri = styleUri,
                    contentUri = contentUri,
                    onSelectStyleImage = { pickStyleLauncher.launch("image/*") },
                    onSelectContentImage = { pickContentLauncher.launch("image/*") },
                )
                val styleTransferResultUiState = uiState.styleTransferResultUiState
                if (styleTransferResultUiState is StyleTransferResultUiState.Success) {
                    ResultArea(bitmap = styleTransferResultUiState.image)
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .padding(paddingValues)
                    .verticalScroll(columnScrollState)
            ) {
                InputArea(
                    styleUri = styleUri,
                    contentUri = contentUri,
                    onSelectStyleImage = { pickStyleLauncher.launch("image/*") },
                    onSelectContentImage = { pickContentLauncher.launch("image/*") },
                )
                val styleTransferResultUiState = uiState.styleTransferResultUiState
                if (styleTransferResultUiState is StyleTransferResultUiState.Success) {
                    ResultArea(bitmap = styleTransferResultUiState.image)
                }
            }
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
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
        )
    }
}

@Composable
private fun InputArea(
    styleUri: Uri?,
    contentUri: Uri?,
    onSelectStyleImage: () -> Unit,
    onSelectContentImage: () -> Unit,
) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(
                if (LocalWindowSizeClass.current.widthSizeClass == WindowWidthSizeClass.Compact) 1f
                else 0.5f
            ),
        horizontalAlignment = Alignment.End,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RaysCard(modifier = Modifier.weight(0.5f), onClick = onSelectStyleImage) {
                RaysImage(
                    model = styleUri,
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
                    text = stringResource(R.string.style_transfer_screen_style)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Icon(imageVector = Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(10.dp))
            RaysCard(modifier = Modifier.weight(0.5f), onClick = onSelectContentImage) {
                RaysImage(
                    model = contentUri,
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
                    text = stringResource(R.string.style_transfer_screen_content)
                )
            }
        }
    }
}

@Composable
private fun TransferExtendedFloatingActionButton(visible: Boolean, onClick: () -> Unit) {
    val density = LocalDensity.current
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically { with(density) { 40.dp.roundToPx() } } + fadeIn(),
        exit = slideOutVertically { with(density) { 40.dp.roundToPx() } } + fadeOut(),
    ) {
        RaysExtendedFloatingActionButton(
            text = { Text(text = stringResource(R.string.style_transfer_screen_transfer)) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Transform,
                    contentDescription = null
                )
            },
            onClick = onClick,
            contentDescription = stringResource(R.string.style_transfer_screen_transfer),
        )
    }
}