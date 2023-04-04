package com.skyd.rays.ui.screen.add

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skyd.rays.R
import com.skyd.rays.appContext
import com.skyd.rays.config.refreshStickerData
import com.skyd.rays.ext.plus
import com.skyd.rays.ext.popBackStackWithLifecycle
import com.skyd.rays.model.bean.StickerBean
import com.skyd.rays.model.bean.StickerWithTags
import com.skyd.rays.model.bean.TagBean
import com.skyd.rays.ui.component.*
import com.skyd.rays.ui.component.dialog.RaysDialog
import com.skyd.rays.ui.local.LocalNavController
import com.skyd.rays.util.stickerUuidToUri
import kotlinx.coroutines.launch

const val ADD_SCREEN_ROUTE = "addScreen"

@Composable
fun AddScreen(stickerUuid: String, sticker: Uri?, viewModel: AddViewModel = hiltViewModel()) {
    var openDialog by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val navController = LocalNavController.current
    var titleText by rememberSaveable { mutableStateOf("") }
    var stickerUri by remember { mutableStateOf<Uri?>(null) }
    val tags = remember { mutableStateListOf<TagBean>() }

    if (stickerUuid.isNotBlank()) {
        LaunchedEffect(Unit) {
            viewModel.sendUiIntent(AddIntent.GetStickerWithTags(stickerUuid))
        }
    } else {
        stickerUri = sticker
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            RaysTopBar(
                title = {
                    Text(
                        text = stringResource(
                            if (stickerUuid.isBlank()) R.string.add_screen_name
                            else R.string.add_screen_name_edit
                        )
                    )
                },
                actions = {
                    TopBarIcon(
                        imageVector = Icons.Default.Done,
                        contentDescription = stringResource(R.string.add_screen_add),
                        onClick = {
                            if (stickerUri == null) {
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        appContext.getString(R.string.add_screen_sticker_is_not_set),
                                        withDismissAction = true
                                    )
                                }
                            } else {
                                keyboardController?.hide()
                                focusManager.clearFocus()
                                val stickerWithTags = StickerWithTags(
                                    sticker = StickerBean(title = titleText)
                                        .apply { uuid = stickerUuid },
                                    tags = tags.toList()
                                )
                                viewModel.sendUiIntent(
                                    AddIntent.AddNewStickerWithTags(stickerWithTags, stickerUri!!)
                                )
                            }
                        }
                    )
                }
            )
        }
    ) { paddingValues ->
        val pickStickerLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.GetContent()
        ) { result ->
            result?.let {
                stickerUri = it
            }
        }
        LazyColumn(contentPadding = paddingValues + PaddingValues(horizontal = 16.dp)) {
            item {
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp)
                        .focusRequester(focusRequester),
                    value = titleText,
                    onValueChange = { titleText = it },
                    label = { Text(stringResource(R.string.add_screen_title)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        focusManager.moveFocus(FocusDirection.Next)
                    })
                )
            }
            item {
                var currentTagText by rememberSaveable { mutableStateOf("") }
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp),
                    value = currentTagText,
                    onValueChange = { currentTagText = it },
                    placeholder = { Text(text = stringResource(R.string.add_screen_tag_field_hint)) },
                    label = { Text(stringResource(R.string.add_screen_add_tags)) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        if (currentTagText.isNotBlank()) {
                            tags.add(TagBean(tag = currentTagText))
                        } else {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                        }
                        currentTagText = ""
                    })
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    repeat(tags.size) { index ->
                        InputChip(
                            selected = false,
                            label = { Text(tags[index].tag) },
                            onClick = { tags.remove(tags[index]) },
                            trailingIcon = {
                                Icon(
                                    modifier = Modifier.size(AssistChipDefaults.IconSize),
                                    imageVector = Icons.Default.Close, contentDescription = null
                                )
                            }
                        )
                    }
                }
            }
            item {
                RaysCard(
                    modifier = Modifier.padding(top = 20.dp),
                    colors = CardDefaults.cardColors(Color.Transparent),
                    onClick = { pickStickerLauncher.launch("image/*") }
                ) {
                    if (stickerUri == null) {
                        Box(modifier = Modifier.aspectRatio(1.6f)) {
                            AnimatedPlaceholder(
                                resId = R.raw.lottie_genshin_impact_diona_1,
                                sizeFraction = 0.9f,
                                tip = stringResource(R.string.add_screen_select_sticker)
                            )
                        }
                    } else {
                        RaysImage(
                            uri = stickerUri,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        RaysDialog(
            visible = openDialog,
            title = { Text(text = stringResource(R.string.dialog_tip)) },
            text = { Text(text = stringResource(R.string.add_screen_success)) },
            onDismissRequest = {
                openDialog = false
                navController.popBackStackWithLifecycle()
            },
            confirmButton = {
                TextButton(onClick = {
                    openDialog = false
                    navController.popBackStackWithLifecycle()
                }) {
                    Text(text = stringResource(id = R.string.dialog_ok))
                }
            }
        )
    }

    viewModel.uiStateFlow.collectAsStateWithLifecycle().value.apply {
        when (getStickersWithTagsUiState) {
            is GetStickersWithTagsUiState.Success -> {
                val stickerBean = getStickersWithTagsUiState.stickerWithTags.sticker
                titleText = stickerBean.title
                stickerUri = stickerUuidToUri(stickerBean.uuid)
                tags.clear()
                tags.addAll(getStickersWithTagsUiState.stickerWithTags.tags)
            }
            GetStickersWithTagsUiState.Failed -> {}
            GetStickersWithTagsUiState.Init -> {}
        }
    }

    viewModel.uiEventFlow.collectAsStateWithLifecycle(initialValue = null).value?.apply {
        when (addStickersResultUiEvent) {
            AddStickersResultUiEvent.Failed -> {
            }
            is AddStickersResultUiEvent.Success -> {
                refreshStickerData.tryEmit(Unit)
                openDialog = true
            }
            null -> {}
        }
    }
}
