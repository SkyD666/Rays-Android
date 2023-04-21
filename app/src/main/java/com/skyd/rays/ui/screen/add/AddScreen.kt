package com.skyd.rays.ui.screen.add

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import com.skyd.rays.ext.addIfAny
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
fun AddScreen(initStickerUuid: String, sticker: Uri?, viewModel: AddViewModel = hiltViewModel()) {
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
    var stickerUuid by remember { mutableStateOf(initStickerUuid) }
    val stickerTexts = remember { mutableStateListOf<String>() }

    if (initStickerUuid.isNotBlank()) {
        LaunchedEffect(Unit) {
            viewModel.sendUiIntent(AddIntent.GetStickerWithTags(initStickerUuid))
        }
    } else if (sticker != null) {
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
                                    tags = tags.distinct()
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
        var currentTagText by rememberSaveable { mutableStateOf("") }
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
                            tags.addIfAny(TagBean(tag = currentTagText)) { it.tag != currentTagText }
                        } else {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                        }
                        currentTagText = ""
                    })
                )
            }
            item {
                AnimatedVisibility(
                    visible = tags.isNotEmpty(),
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut(),
                ) {
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
                                        imageVector = Icons.Default.Close,
                                        contentDescription = null
                                    )
                                }
                            )
                        }
                    }
                }
            }
            item {
                AnimatedVisibility(
                    visible = stickerTexts.isNotEmpty(),
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut(),
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = stringResource(R.string.add_screen_suggest_tag),
                            style = MaterialTheme.typography.titleMedium
                        )
                        FlowRow(
                            modifier = Modifier
                                .padding(top = 5.dp)
                                .fillMaxWidth()
                                .heightIn(max = 100.dp)
                                .verticalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                        ) {
                            repeat(stickerTexts.size) { index ->
                                SuggestionChip(
                                    modifier = Modifier.combinedClickable { },
                                    label = { Text(stickerTexts[index]) },
                                    onClick = {
                                        val text = stickerTexts[index]
                                        stickerTexts.removeAt(index)
                                        currentTagText = text
                                        tags.addIfAny(TagBean(tag = text)) { it.tag != text }
                                    },
                                )
                            }
                        }
                    }
                }
            }
            item {
                RaysCard(
                    modifier = Modifier.padding(vertical = 20.dp),
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
                        LaunchedEffect(stickerUri) {
                            viewModel.sendUiIntent(AddIntent.GetSuggestTags(stickerUri!!))
                        }
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

        viewModel.uiStateFlow.collectAsStateWithLifecycle().value.apply {
            when (getStickersWithTagsUiState) {
                is GetStickersWithTagsUiState.Success -> {
                    val stickerBean = getStickersWithTagsUiState.stickerWithTags.sticker
                    stickerUuid = stickerBean.uuid
                    titleText = stickerBean.title
                    stickerUri = stickerUuidToUri(stickerBean.uuid)
                    tags.clear()
                    tags.addAll(getStickersWithTagsUiState.stickerWithTags.tags.distinct())
                }
                GetStickersWithTagsUiState.Failed -> {}
                GetStickersWithTagsUiState.Init -> {}
            }
        }
    }


    viewModel.uiEventFlow.collectAsStateWithLifecycle(initialValue = null).value?.apply {
        when (addStickersResultUiEvent) {
            AddStickersResultUiEvent.Duplicate -> {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        appContext.getString(R.string.add_screen_sticker_duplicate),
                        withDismissAction = true
                    )
                }
            }
            is AddStickersResultUiEvent.Success -> {
                refreshStickerData.tryEmit(Unit)
                openDialog = true
            }
            null -> {}
        }
        when (recognizeTextUiEvent) {
            is RecognizeTextUiEvent.Success -> {
                stickerTexts.clear()
                stickerTexts += recognizeTextUiEvent.texts
            }
            null -> {}
        }
    }
}
