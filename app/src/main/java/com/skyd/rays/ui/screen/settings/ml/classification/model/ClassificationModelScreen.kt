package com.skyd.rays.ui.screen.settings.ml.classification.model

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CreateNewFolder
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.LightbulbCircle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.skyd.rays.base.mvi.MviEventListener
import com.skyd.rays.base.mvi.getDispatcher
import com.skyd.rays.ext.safeLaunch
import com.skyd.rays.model.bean.ModelBean
import com.skyd.rays.model.preference.StickerClassificationModelPreference
import com.skyd.rays.ui.component.BaseSettingsItem
import com.skyd.rays.ui.component.RadioSettingsItem
import com.skyd.rays.ui.component.RaysSwipeToDismiss
import com.skyd.rays.ui.component.RaysTopBar
import com.skyd.rays.ui.component.RaysTopBarStyle
import com.skyd.rays.ui.component.dialog.DeleteWarningDialog
import com.skyd.rays.ui.component.dialog.WaitingDialog
import com.skyd.rays.ui.local.LocalStickerClassificationModel
import kotlinx.serialization.Serializable


@Serializable
data object ClassificationModelRoute

@Composable
fun ClassificationModelScreen(viewModel: ClassificationModelViewModel = hiltViewModel()) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var openDeleteWarningDialog by remember { mutableStateOf<ModelBean?>(null) }
    val uiState by viewModel.viewState.collectAsStateWithLifecycle()
    val dispatch = viewModel.getDispatcher(startWith = ClassificationModelIntent.GetModels)

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
            classificationModelState = uiState,
            onDelete = { openDeleteWarningDialog = it },
            onImportModel = { uri ->
                dispatch(ClassificationModelIntent.ImportModel(uri))
            },
            onSetModel = { model ->
                dispatch(ClassificationModelIntent.SetModel(model))
            }
        )

        MviEventListener(viewModel.singleEvent) { event ->
            when (event) {
                is ClassificationModelEvent.DeleteEvent.Failed -> snackbarHostState.showSnackbar(
                    context.getString(R.string.failed_info, event.msg),
                )

                is ClassificationModelEvent.ImportEvent.Failed -> snackbarHostState.showSnackbar(
                    context.getString(R.string.failed_info, event.msg),
                )

                is ClassificationModelEvent.DeleteEvent.Success,
                is ClassificationModelEvent.ImportEvent.Success -> Unit
            }
        }

        WaitingDialog(visible = uiState.loadingDialog && openDeleteWarningDialog == null)

        DeleteWarningDialog(
            visible = openDeleteWarningDialog != null,
            onDismissRequest = { openDeleteWarningDialog = null },
            onDismiss = { openDeleteWarningDialog = null },
            onConfirm = {
                dispatch(ClassificationModelIntent.DeleteModel(openDeleteWarningDialog!!))
                openDeleteWarningDialog = null
            }
        )
    }
}

@Composable
private fun ModelList(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    classificationModelState: ClassificationModelState,
    onDelete: (ModelBean) -> Unit,
    onImportModel: (Uri) -> Unit,
    onSetModel: (ModelBean) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val classificationModel = LocalStickerClassificationModel.current
    val classificationModelName = rememberSaveable(classificationModel) {
        classificationModel.substringAfterLast("/")
    }
    val pickModelLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            onImportModel(uri)
        }
    }
    val getModelsUiState = classificationModelState.getModelsState
    val models = if (getModelsUiState is GetModelsState.Success) {
        getModelsUiState.models
    } else {
        emptyList()
    }
    LazyColumn(
        modifier = modifier,
        contentPadding = contentPadding,
    ) {
        item {
            RadioSettingsItem(
                selected = classificationModel.isBlank(),
                imageVector = Icons.Outlined.LightbulbCircle,
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
                painter = rememberVectorPainter(image = Icons.Outlined.CreateNewFolder),
                text = stringResource(id = R.string.classification_model_screen_select),
                descriptionText = stringResource(
                    R.string.classification_model_screen_select_description,
                ),
                onClick = { pickModelLauncher.safeLaunch("application/octet-stream") }
            )
        }
        itemsIndexed(models) { _, item ->
            RaysSwipeToDismiss(
                state = rememberSwipeToDismissBoxState(
                    confirmValueChange = { dismissValue ->
                        if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                            onDelete(item)
                        }
                        false
                    }
                ),
                enableDismissFromStartToEnd = false,
                enableDismissFromEndToStart = true,
            ) {
                RadioSettingsItem(
                    modifier = Modifier.background(MaterialTheme.colorScheme.background),
                    selected = item.name == classificationModelName,
                    painter = rememberVectorPainter(image = Icons.Outlined.Lightbulb),
                    text = item.name,
                    description = item.path,
                    onClick = {
                        if (item.name != classificationModelName) {
                            onSetModel(item)
                        }
                    }
                )
            }
        }
    }
}
