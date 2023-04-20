package com.skyd.rays.ui.screen.settings.ml.classification

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LightbulbCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skyd.rays.R
import com.skyd.rays.appContext
import com.skyd.rays.base.LoadUiIntent
import com.skyd.rays.model.preference.StickerClassificationModelPreference
import com.skyd.rays.ui.component.BaseSettingsItem
import com.skyd.rays.ui.component.RadioSettingsItem
import com.skyd.rays.ui.component.RaysTopBar
import com.skyd.rays.ui.component.RaysTopBarStyle
import com.skyd.rays.ui.component.dialog.DeleteWarningDialog
import com.skyd.rays.ui.component.dialog.WaitingDialog
import com.skyd.rays.ui.local.LocalStickerClassificationModel
import kotlinx.coroutines.launch


const val CLASSIFICATION_MODEL_SCREEN_ROUTE = "classificationModelScreen"

@Composable
fun ClassificationModelScreen(viewModel: ClassificationModelViewModel = hiltViewModel()) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val context = LocalContext.current
    val classificationModel = LocalStickerClassificationModel.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var modelUri by remember { mutableStateOf<Uri?>(null) }
    var openWaitingDialog by remember { mutableStateOf(false) }
    var openDeleteWarningDialog by remember { mutableStateOf<Uri?>(null) }
    val pickModelLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { result ->
        result?.let {
            modelUri = it
            viewModel.sendUiIntent(ClassificationModelIntent.ImportModel(it))
        }
    }

    LaunchedEffect(Unit) {
        viewModel.sendUiIntent(ClassificationModelIntent.GetModels)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            RaysTopBar(
                style = RaysTopBarStyle.Large,
                scrollBehavior = scrollBehavior,
                title = { Text(text = stringResource(R.string.classification_model_screen_name)) },
            )
        }
    ) { paddingValues ->
        val models = remember { mutableStateListOf<Uri>() }

        viewModel.uiStateFlow.collectAsStateWithLifecycle().value.apply {
            when (getModelsUiState) {
                GetModelsUiState.Init -> {}
                is GetModelsUiState.Success -> {
                    models.clear()
                    models += getModelsUiState.models
                }
            }
        }

        viewModel.loadUiIntentFlow.collectAsStateWithLifecycle(initialValue = null).value?.also {
            when (it) {
                is LoadUiIntent.Error -> {
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = appContext.getString(
                                R.string.classification_model_screen_failed, it.msg
                            ),
                            withDismissAction = true
                        )
                    }
                }
                is LoadUiIntent.Loading -> {
                    openWaitingDialog = it.isShow
                }
                LoadUiIntent.ShowMainView -> {}
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = paddingValues
        ) {
            item {
                RadioSettingsItem(
                    selected = classificationModel.isBlank(),
                    icon = Icons.Default.LightbulbCircle,
                    text = stringResource(id = R.string.classification_model_screen_default),
                    description = stringResource(id = R.string.classification_model_screen_default_name),
                    onClick = {
                        if (classificationModel.isNotBlank()) {
                            StickerClassificationModelPreference.put(
                                context = context,
                                scope = scope,
                                value = StickerClassificationModelPreference.default
                            )
                        }
                    }
                )
            }
            item {
                BaseSettingsItem(
                    icon = rememberVectorPainter(image = Icons.Default.CreateNewFolder),
                    text = stringResource(id = R.string.classification_model_screen_select),
                    descriptionText = stringResource(
                        R.string.classification_model_screen_select_description,
                    ),
                    onClick = { pickModelLauncher.launch("*/tflite;*/lite") }
                )
            }
            itemsIndexed(models) { index, item ->
                val path = remember { item.path }
                val name = remember { path?.substringAfterLast("/").orEmpty() }
                RadioSettingsItem(
                    selected = name == classificationModel.substringAfterLast("/"),
                    icon = rememberVectorPainter(image = Icons.Default.Lightbulb),
                    text = name,
                    description = path,
                    onLongClick = { openDeleteWarningDialog = item },
                    onClick = {
                        if (name != classificationModel.substringAfterLast("/") &&
                            path != null
                        ) {
                            viewModel.sendUiIntent(ClassificationModelIntent.SetModel(item))
                        }
                    }
                )
            }
        }

        WaitingDialog(visible = openWaitingDialog)

        DeleteWarningDialog(
            visible = openDeleteWarningDialog != null,
            onDismissRequest = { openDeleteWarningDialog = null },
            onDismiss = { openDeleteWarningDialog = null },
            onConfirm = {
                viewModel.sendUiIntent(ClassificationModelIntent.DeleteModel(openDeleteWarningDialog!!))
                openDeleteWarningDialog = null
            }
        )
    }
}
