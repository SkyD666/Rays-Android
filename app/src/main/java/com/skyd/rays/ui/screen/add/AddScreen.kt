package com.skyd.rays.ui.screen.add

import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skyd.rays.R
import com.skyd.rays.appContext
import com.skyd.rays.base.LoadUiIntent
import com.skyd.rays.config.refreshStickerData
import com.skyd.rays.ext.addAllDistinctly
import com.skyd.rays.ext.addIfAny
import com.skyd.rays.ext.plus
import com.skyd.rays.ext.popBackStackWithLifecycle
import com.skyd.rays.ext.showSnackbar
import com.skyd.rays.model.bean.StickerBean
import com.skyd.rays.model.bean.StickerWithTags
import com.skyd.rays.model.bean.TagBean
import com.skyd.rays.ui.component.AnimatedPlaceholder
import com.skyd.rays.ui.component.RaysCard
import com.skyd.rays.ui.component.RaysImage
import com.skyd.rays.ui.component.RaysTopBar
import com.skyd.rays.ui.component.TopBarIcon
import com.skyd.rays.ui.component.dialog.RaysDialog
import com.skyd.rays.ui.local.LocalNavController
import com.skyd.rays.util.stickerUuidToUri

const val ADD_SCREEN_ROUTE = "addScreen"

@Composable
fun AddScreen(initStickerUuid: String, sticker: Uri?, viewModel: AddViewModel = hiltViewModel()) {
    var openDialog by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val navController = LocalNavController.current
    var titleText by rememberSaveable { mutableStateOf("") }
    var currentTagText by rememberSaveable { mutableStateOf("") }
    var stickerUri by remember { mutableStateOf<Uri?>(null) }
    var stickerCreateTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val tags = remember { mutableStateListOf<TagBean>() }
    var stickerUuid by remember { mutableStateOf(initStickerUuid) }
    val suggestedTags = remember { mutableStateListOf<String>() }
    val loadUiIntent by viewModel.loadUiIntentFlow.collectAsStateWithLifecycle(initialValue = null)
    val uiEvent by viewModel.uiEventFlow.collectAsStateWithLifecycle(initialValue = null)
    val uriWaitingList = remember { mutableStateListOf<Uri>() }

    LaunchedEffect(Unit) {
        if (initStickerUuid.isNotBlank()) {
            viewModel.sendUiIntent(AddIntent.GetStickerWithTags(initStickerUuid))
        } else if (sticker != null) {
            stickerUri = sticker
        }
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
                        imageVector = if (uriWaitingList.isEmpty()) {
                            Icons.Default.Done
                        } else {
                            Icons.Default.ArrowForwardIos
                        },
                        contentDescription = stringResource(R.string.add_screen_add),
                        onClick = {
                            if (stickerUri == null) {
                                snackbarHostState.showSnackbar(
                                    scope = scope,
                                    message = appContext.getString(R.string.add_screen_sticker_is_not_set),
                                )
                            } else {
                                keyboardController?.hide()
                                focusManager.clearFocus()
                                val stickerWithTags = StickerWithTags(
                                    sticker = StickerBean(
                                        title = titleText,
                                        createTime = stickerCreateTime
                                    ).apply { uuid = stickerUuid },
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
        val pickStickerLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.GetContent()
        ) { result ->
            if (result != null) stickerUri = result
        }
        val pickStickersLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.GetMultipleContents()
        ) { result ->
            if (result.isEmpty()) return@rememberLauncherForActivityResult
            if (stickerUri == null) {
                stickerUri = result.first()
                uriWaitingList.addAllDistinctly(result.subList(1, result.size))
            } else {
                uriWaitingList.addAllDistinctly(result)
                uriWaitingList.removeIf { it == stickerUri }
            }
        }
        LazyColumn(contentPadding = paddingValues + PaddingValues(horizontal = 16.dp)) {
            item {
                AnimatedVisibility(
                    visible = uriWaitingList.isNotEmpty(),
                    enter = expandVertically(),
                    exit = shrinkVertically(),
                ) {
                    WaitingRow(
                        uris = uriWaitingList,
                        onStickerDeleted = { uriWaitingList.remove(it) },
                    )
                }
            }
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
                    singleLine = true,
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
                    enter = expandVertically(),
                    exit = shrinkVertically(),
                ) {
                    AddedTags(tags = tags, onClick = { tags.remove(tags[it]) })
                }
            }
            item {
                SuggestedTags(suggestedTags = suggestedTags, onClick = { index ->
                    val text = suggestedTags.removeAt(index)
                    currentTagText = text
                    tags.addIfAny(TagBean(tag = text)) { it.tag != text }
                })
            }
            item {
                StickerCard(
                    stickerUri = stickerUri,
                    pickLauncher = if (initStickerUuid.isBlank()) {
                        pickStickersLauncher
                    } else {
                        pickStickerLauncher
                    }
                )
            }
        }

        RaysDialog(
            visible = openDialog,
            title = { Text(text = stringResource(R.string.dialog_tip)) },
            text = { Text(text = stringResource(R.string.add_screen_success)) },
            onDismissRequest = {
                openDialog = false
                if (uriWaitingList.isEmpty() && stickerUri == null) {
                    navController.popBackStackWithLifecycle()
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    openDialog = false
                    if (uriWaitingList.isEmpty() && stickerUri == null) {
                        navController.popBackStackWithLifecycle()
                    }
                }) {
                    Text(text = stringResource(id = R.string.dialog_ok))
                }
            }
        )
    }

    // 添加/修改完成后重设页面数据
    fun resetStickerData() {
        stickerUuid = initStickerUuid
        titleText = ""
        currentTagText = ""
        stickerCreateTime = System.currentTimeMillis()
        suggestedTags.clear()
        tags.clear()
        if (uriWaitingList.isEmpty()) {
            stickerUri = null
        } else {
            stickerUri = uriWaitingList.first()
            uriWaitingList.removeAt(0)
        }
    }

    uiEvent?.apply {
        when (getStickersWithTagsUiEvent) {
            is GetStickersWithTagsUiEvent.Success -> {
                val stickerBean = getStickersWithTagsUiEvent.stickerWithTags.sticker
                stickerUuid = stickerBean.uuid
                titleText = stickerBean.title
                stickerUri = stickerUuidToUri(stickerBean.uuid)
                stickerCreateTime = stickerBean.createTime
                tags.clear()
                tags.addAll(getStickersWithTagsUiEvent.stickerWithTags.tags.distinct())
            }

            GetStickersWithTagsUiEvent.Init,
            GetStickersWithTagsUiEvent.Failed,
            null -> Unit
        }

        when (addStickersResultUiEvent) {
            AddStickersResultUiEvent.Duplicate -> {
                LaunchedEffect(snackbarHostState) {
                    snackbarHostState.showSnackbar(
                        context.getString(R.string.add_screen_sticker_duplicate),
                        withDismissAction = true
                    )
                }
            }

            is AddStickersResultUiEvent.Success -> {
                refreshStickerData.tryEmit(Unit)
                LaunchedEffect(Unit) {
                    resetStickerData()
                }
                openDialog = true
            }

            null -> Unit
        }
        when (recognizeTextUiEvent) {
            is RecognizeTextUiEvent.Success -> {
                suggestedTags.clear()
                suggestedTags += recognizeTextUiEvent.texts
            }

            null -> Unit
        }
    }

    loadUiIntent?.also {
        when (it) {
            is LoadUiIntent.Error -> {
                LaunchedEffect(snackbarHostState) {
                    snackbarHostState.showSnackbar(
                        context.getString(R.string.add_screen_error, it.msg),
                        withDismissAction = true
                    )
                }
            }

            is LoadUiIntent.Loading -> Unit
        }
    }
}

@Composable
private fun WaitingRow(uris: List<Uri>, onStickerDeleted: (Uri) -> Unit) {
    Column {
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = stringResource(R.string.add_screen_waiting_uris),
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(10.dp))
        LazyRow(
            modifier = Modifier.height(100.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            items(uris) { uri ->
                RaysCard(onClick = { onStickerDeleted(uri) }) {
                    RaysImage(
                        model = uri,
                        modifier = Modifier
                            .fillMaxHeight()
                            .aspectRatio(1f),
                        contentScale = ContentScale.Crop,
                    )
                }
            }
        }
    }
}

@Composable
private fun StickerCard(
    stickerUri: Uri?,
    pickLauncher: ManagedActivityResultLauncher<String, *>,
    viewModel: AddViewModel = hiltViewModel(),
) {
    RaysCard(
        modifier = Modifier.padding(vertical = 20.dp),
        colors = CardDefaults.cardColors(Color.Transparent),
        onClick = { pickLauncher.launch("image/*") },
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
                viewModel.sendUiIntent(AddIntent.GetSuggestTags(stickerUri))
            }
            RaysImage(
                model = stickerUri,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun AddedTags(tags: List<TagBean>, onClick: (Int) -> Unit) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        repeat(tags.size) { index ->
            InputChip(
                selected = false,
                label = { Text(tags[index].tag) },
                onClick = { onClick(index) },
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

@Composable
private fun SuggestedTags(suggestedTags: List<String>, onClick: (Int) -> Unit) {
    AnimatedVisibility(
        visible = suggestedTags.isNotEmpty(),
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
                repeat(suggestedTags.size) { index ->
                    SuggestionChip(
                        modifier = Modifier.combinedClickable { },
                        label = { Text(suggestedTags[index]) },
                        onClick = {
                            onClick(index)
                        },
                    )
                }
            }
        }
    }
}