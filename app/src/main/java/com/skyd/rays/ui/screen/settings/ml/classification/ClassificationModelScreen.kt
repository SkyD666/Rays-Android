package com.skyd.rays.ui.screen.settings.ml.classification

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LightbulbCircle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skyd.rays.R
import com.skyd.rays.base.LoadUiIntent
import com.skyd.rays.ext.addIfAny
import com.skyd.rays.model.bean.ModelBean
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
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var openWaitingDialog by remember { mutableStateOf(false) }
    var openDeleteWarningDialog by remember { mutableStateOf<ModelBean?>(null) }
    val models = remember { mutableStateListOf<ModelBean>() }

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
        ModelList(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = paddingValues,
            models = models,
            onDelete = { openDeleteWarningDialog = it }
        )

        viewModel.uiStateFlow.collectAsStateWithLifecycle().value.apply {
            when (getModelsUiState) {
                GetModelsUiState.Init -> {
                    viewModel.sendUiIntent(ClassificationModelIntent.GetModels)
                }

                is GetModelsUiState.Success -> {
                    getModelsUiState.models.forEach { newModel ->
                        models.addIfAny(newModel) { it.path != newModel.path }
                    }
                    models.removeIf { model ->
                        getModelsUiState.models.firstOrNull { it.path == model.path } == null
                    }
                }
            }
        }

        viewModel.uiEventFlow.collectAsStateWithLifecycle(initialValue = null).value?.apply {
            when (importUiEvent) {
                is ImportUiEvent.Success -> {
                    viewModel.sendUiIntent(ClassificationModelIntent.GetModels)
                }

                null -> {}
            }
            when (deleteUiEvent) {
                is DeleteUiEvent.Success -> {
                    viewModel.sendUiIntent(ClassificationModelIntent.GetModels)
                }

                null -> {}
            }
        }

        viewModel.loadUiIntentFlow.collectAsStateWithLifecycle(initialValue = null).value?.also {
            when (it) {
                is LoadUiIntent.Error -> {
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = context.getString(
                                R.string.classification_model_screen_failed,
                                it.msg
                            ),
                            withDismissAction = true
                        )
                    }
                }

                is LoadUiIntent.Loading -> {
                    openWaitingDialog = it.isShow
                }
            }
        }

        WaitingDialog(visible = openWaitingDialog && openDeleteWarningDialog == null)

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

@Composable
private fun ModelList(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    models: List<ModelBean>,
    onDelete: (ModelBean) -> Unit,
    viewModel: ClassificationModelViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val classificationModel = LocalStickerClassificationModel.current
    val classificationModelName = classificationModel.substringAfterLast("/")
    val pickModelLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.sendUiIntent(ClassificationModelIntent.ImportModel(uri))
        }
    }
    LazyColumn(
        modifier = modifier,
        contentPadding = contentPadding,
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
                onClick = { pickModelLauncher.launch("application/octet-stream") }
            )
        }
        itemsIndexed(models) { _, item ->
            RadioSettingsItem(
                selected = item.name == classificationModelName,
                icon = rememberVectorPainter(image = Icons.Default.Lightbulb),
                text = item.name,
                description = item.path,
                onLongClick = { onDelete(item) },
                onClick = {
                    if (item.name != classificationModelName) {
                        viewModel.sendUiIntent(ClassificationModelIntent.SetModel(item))
                    }
                }
            )
        }
    }
}
