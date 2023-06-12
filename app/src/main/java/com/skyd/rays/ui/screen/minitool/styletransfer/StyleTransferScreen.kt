package com.skyd.rays.ui.screen.minitool.styletransfer

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skyd.rays.R
import com.skyd.rays.base.LoadUiIntent
import com.skyd.rays.ui.component.RaysCard
import com.skyd.rays.ui.component.RaysImage
import com.skyd.rays.ui.component.RaysTopBar
import com.skyd.rays.ui.component.dialog.WaitingDialog
import com.skyd.rays.ui.local.LocalWindowSizeClass
import com.skyd.rays.util.sendSticker

const val STYLE_TRANSFER_SCREEN_ROUTE = "styleTransferScreen"

@Composable
fun StyleTransferScreen(viewModel: StyleTransferViewModel = hiltViewModel()) {
    var openWaitingDialog by remember { mutableStateOf(false) }
    val uiState by viewModel.uiStateFlow.collectAsStateWithLifecycle()
    val loadUiIntentFlow by viewModel.loadUiIntentFlow.collectAsStateWithLifecycle(initialValue = null)

    Scaffold(
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
                    .verticalScroll(rememberScrollState())
            ) {
                InputArea()
                val styleTransferResultUiState = uiState.styleTransferResultUiState
                if (styleTransferResultUiState is StyleTransferResultUiState.Success) {
                    ResultArea(bitmap = styleTransferResultUiState.image)
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                InputArea()
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
private fun InputArea(viewModel: StyleTransferViewModel = hiltViewModel()) {
    var styleUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var contentUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    val pickStyleLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { if (it != null) styleUri = it }
    val pickContentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { if (it != null) contentUri = it }
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.End,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RaysCard(modifier = Modifier
                .weight(0.5f)
                .aspectRatio(1f),
                onClick = { pickStyleLauncher.launch("image/*") }
            ) {
                RaysImage(
                    model = styleUri,
                    modifier = Modifier.fillMaxWidth(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Icon(imageVector = Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(10.dp))
            RaysCard(
                modifier = Modifier
                    .weight(0.5f)
                    .aspectRatio(1f),
                onClick = { pickContentLauncher.launch("image/*") }
            ) {
                RaysImage(
                    model = contentUri,
                    modifier = Modifier.fillMaxWidth(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Button(
            enabled = styleUri != null && contentUri != null,
            onClick = {
                viewModel.sendUiIntent(
                    StyleTransferIntent.Transfer(
                        style = styleUri!!,
                        content = contentUri!!,
                    )
                )
            }
        ) {
            Text(text = stringResource(R.string.style_transfer_screen_transfer))
        }
    }
}