package com.skyd.rays.ui.screen.add

import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AddToPhotos
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditOff
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RichTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.skyd.rays.R
import com.skyd.rays.base.mvi.getDispatcher
import com.skyd.rays.ext.navigate
import com.skyd.rays.ext.popBackStackWithLifecycle
import com.skyd.rays.ext.showSnackbar
import com.skyd.rays.ext.showSnackbarWithLaunchedEffect
import com.skyd.rays.model.bean.StickerBean
import com.skyd.rays.model.bean.StickerWithTags
import com.skyd.rays.model.bean.TagBean
import com.skyd.rays.model.bean.UriWithStickerUuidBean
import com.skyd.rays.ui.component.RaysIconButton
import com.skyd.rays.ui.component.RaysImage
import com.skyd.rays.ui.component.RaysTopBar
import com.skyd.rays.ui.component.dialog.RaysDialog
import com.skyd.rays.ui.component.dialog.WaitingDialog
import com.skyd.rays.ui.local.LocalNavController
import kotlinx.coroutines.launch

const val ADD_SCREEN_ROUTE = "addScreen"

fun openAddScreen(
    navController: NavHostController,
    stickers: MutableList<UriWithStickerUuidBean>,
    isEdit: Boolean
) {
    navController.navigate(
        ADD_SCREEN_ROUTE,
        Bundle().apply {
            putBoolean("isEdit", isEdit)
            putParcelableArrayList("stickers", ArrayList(stickers))
        }
    )
}

@Composable
fun AddScreen(
    initStickers: MutableList<UriWithStickerUuidBean>,
    isEdit: Boolean,
    viewModel: AddViewModel = hiltViewModel()
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val navController = LocalNavController.current
    var titleText by rememberSaveable { mutableStateOf("") }
    var currentTagText by rememberSaveable { mutableStateOf("") }
    var stickerCreateTime by rememberSaveable { mutableLongStateOf(System.currentTimeMillis()) }
    val uiState by viewModel.viewState.collectAsStateWithLifecycle()
    val uiEvent by viewModel.singleEvent.collectAsStateWithLifecycle(initialValue = null)

    val dispatch = viewModel.getDispatcher(startWith = AddIntent.Init(initStickers))

    // 添加/修改完成后重设页面数据
    fun resetStickerData() {
        titleText = ""
        currentTagText = ""
        stickerCreateTime = System.currentTimeMillis()
    }

    fun processNext() {
        if (uiState.waitingList.size <= 1) {
            if (isEdit) {
                navController.popBackStackWithLifecycle()
            }
        }
        dispatch(AddIntent.ProcessNext(uiState.waitingList.getOrNull(1)))
    }

    val pickStickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { result ->
        if (result != null) {
            val waitingList = uiState.waitingList
            if (waitingList.isNotEmpty()) {
                dispatch(AddIntent.ReplaceWaitingListFirst(waitingList[0].copy(uri = result)))
            }
        }
    }
    val pickStickersLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { result ->
        if (result.isEmpty()) return@rememberLauncherForActivityResult
        dispatch(AddIntent.AddToWaitingList(result.map { UriWithStickerUuidBean(uri = it) }))
    }

    Scaffold(
        modifier = Modifier.imePadding(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            RaysTopBar(
                title = {
                    Text(
                        stringResource(
                            if (isEdit) R.string.add_screen_name_edit
                            else R.string.add_screen_name
                        )
                    )
                },
                actions = {
                    RaysIconButton(
                        onClick = {
                            (if (isEdit) pickStickerLauncher else pickStickersLauncher)
                                .launch("image/*")
                        },
                        contentDescription = if (isEdit) stringResource(R.string.add_screen_update_sticker)
                        else stringResource(R.string.add_screen_add_stickers),
                        imageVector = if (isEdit) Icons.Default.Image else Icons.Default.AddPhotoAlternate,
                    )
                    RaysIconButton(
                        onClick = {
                            resetStickerData()
                            processNext()
                        },
                        contentDescription = stringResource(R.string.add_screen_skip_current_sticker),
                        imageVector = Icons.Default.EditOff,
                    )
                    RaysIconButton(
                        onClick = {
                            if (uiState.currentSticker == null) {
                                snackbarHostState.showSnackbar(
                                    scope = scope,
                                    message = context.getString(R.string.add_screen_sticker_is_not_set),
                                )
                            } else {
                                keyboardController?.hide()
                                focusManager.clearFocus()
                                val getStickersWithTagsState = uiState.getStickersWithTagsState
                                val stickerBean =
                                    if (getStickersWithTagsState is GetStickersWithTagsState.Success) {
                                        getStickersWithTagsState.stickerWithTags.sticker
                                    } else StickerBean(
                                        title = titleText,
                                        createTime = stickerCreateTime
                                    )
                                val stickerWithTags = StickerWithTags(
                                    sticker = stickerBean.copy(
                                        title = titleText,
                                        createTime = stickerCreateTime
                                    ).apply {
                                        uuid = uiState.currentSticker?.stickerUuid.orEmpty()
                                            .ifBlank {
                                                (uiState.getStickersWithTagsState as?
                                                        GetStickersWithTagsState.Success)
                                                    ?.stickerWithTags?.sticker?.uuid.orEmpty()
                                            }
                                    },
                                    tags = uiState.addedTags.distinct().map { TagBean(tag = it) }
                                )
                                dispatch(
                                    AddIntent.AddNewStickerWithTags(
                                        stickerWithTags,
                                        uiState.currentSticker!!.uri!!
                                    )
                                )
                            }
                        },
                        contentDescription = stringResource(R.string.add_screen_save_current_sticker),
                        imageVector = Icons.Default.Save,
                    )
                }
            )
        },
    ) { paddingValues ->
        var openDialog by remember { mutableStateOf(false) }
        var openDuplicateDialog by remember { mutableStateOf(false) }

        LazyColumn(contentPadding = paddingValues) {
            item {
                Column {
                    WaitingRow(
                        uris = uiState.waitingList,
                        onSelectStickersClick = { pickStickersLauncher.launch("image/*") },
                        onSelectFirstStickerClick = { pickStickerLauncher.launch("image/*") },
                    )
                    AnimatedVisibility(
                        visible = uiState.waitingList.isNotEmpty(),
                        enter = expandVertically(),
                        exit = shrinkVertically(),
                    ) {
                        AddToAllList(
                            list = uiState.addToAllTags,
                            onDeleteTag = { dispatch(AddIntent.RemoveAddToAllTag(it)) }
                        )
                    }
                }
            }
            titleInputFieldItem(
                value = titleText,
                onValueChange = { titleText = it },
            )
            tagsInputFieldItem(
                value = currentTagText,
                onValueChange = { currentTagText = it },
                onAddClick = { dispatch(AddIntent.AddTag(currentTagText)) },
                onAddToAllClick = { dispatch(AddIntent.AddAddToAllTag(currentTagText)) }
            )
            item {
                AnimatedVisibility(
                    visible = uiState.addedTags.isNotEmpty(),
                    enter = expandVertically(),
                    exit = shrinkVertically(),
                ) {
                    AddedTags(tags = uiState.addedTags, onClick = { index ->
                        dispatch(AddIntent.RemoveTag(uiState.addedTags[index]))
                    })
                }
            }
            item {
                SuggestedTags(suggestedTags = uiState.suggestTags, onClick = { index ->
                    currentTagText = uiState.suggestTags[index]
                    dispatch(AddIntent.AddTag(uiState.suggestTags[index]))
                    dispatch(AddIntent.RemoveSuggestTag(uiState.suggestTags[index]))
                })
            }
        }

        fun onGetStickersWithTagsStateChanged() {
            val getStickersWithTagsState = uiState.getStickersWithTagsState
            if (getStickersWithTagsState is GetStickersWithTagsState.Success) {
                titleText = getStickersWithTagsState.stickerWithTags.sticker.title
                stickerCreateTime = getStickersWithTagsState.stickerWithTags.sticker.createTime
            }
        }

        when (val event = uiEvent) {
            is AddEvent.AddStickersResultEvent.Duplicate -> LaunchedEffect(event) {
                openDuplicateDialog = true
                onGetStickersWithTagsStateChanged()
            }

            is AddEvent.AddStickersResultEvent.Failed -> {
                snackbarHostState.showSnackbarWithLaunchedEffect(
                    message = context.getString(R.string.failed_info, event.msg),
                    key1 = event,
                )
            }

            is AddEvent.AddStickersResultEvent.Success -> LaunchedEffect(event) {
                resetStickerData()
                processNext()
                openDialog = true
            }

            AddEvent.CurrentStickerChanged -> {
                LaunchedEffect(uiState.currentSticker) {
                    val currentSticker = uiState.currentSticker
                    resetStickerData()
                    onGetStickersWithTagsStateChanged()
                    if (currentSticker != null) {
                        currentSticker.uri?.let { uri ->
                            dispatch(AddIntent.GetSuggestTags(uri))
                        }
                    } else {
                        processNext()
                    }
                }
            }

            AddEvent.GetStickersWithTagsStateChanged -> LaunchedEffect(uiState.getStickersWithTagsState) {
                onGetStickersWithTagsStateChanged()
            }

            null -> Unit
        }

        RaysDialog(
            visible = openDuplicateDialog,
            title = { Text(text = stringResource(R.string.info)) },
            text = { Text(text = stringResource(R.string.add_screen_sticker_duplicate)) },
            onDismissRequest = { openDuplicateDialog = false },
            confirmButton = {
                TextButton(onClick = { openDuplicateDialog = false }) {
                    Text(text = stringResource(id = R.string.dialog_ok))
                }
            }
        )

        RaysDialog(
            visible = openDialog,
            title = { Text(text = stringResource(R.string.info)) },
            text = { Text(text = stringResource(R.string.add_screen_success)) },
            onDismissRequest = {
                openDialog = false
                if (uiState.waitingList.isEmpty()) {
                    navController.popBackStackWithLifecycle()
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    openDialog = false
                    if (uiState.waitingList.isEmpty()) {
                        navController.popBackStackWithLifecycle()
                    }
                }) {
                    Text(text = stringResource(id = R.string.dialog_ok))
                }
            }
        )
    }

    WaitingDialog(visible = uiState.loadingDialog)
}

private fun LazyListScope.titleInputFieldItem(
    value: String,
    onValueChange: (String) -> Unit,
) {
    item {
        val focusRequester = remember { FocusRequester() }
        val focusManager = LocalFocusManager.current

        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 7.dp)
                .focusRequester(focusRequester),
            value = value,
            onValueChange = onValueChange,
            label = { Text(stringResource(R.string.add_screen_title)) },
            singleLine = true,
            trailingIcon = {
                if (value.isNotEmpty()) {
                    RaysIconButton(
                        onClick = { onValueChange("") },
                        imageVector = Icons.Default.Cancel,
                        contentDescription = stringResource(R.string.cancel),
                    )
                }
            },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done,
                keyboardType = KeyboardType.Uri
            ),
            keyboardActions = KeyboardActions(onDone = {
                focusManager.moveFocus(FocusDirection.Next)
            })
        )

        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }
    }
}

private fun LazyListScope.tagsInputFieldItem(
    value: String,
    onValueChange: (String) -> Unit,
    onAddClick: () -> Unit,
    onAddToAllClick: () -> Unit,
) {
    item {
        val keyboardController = LocalSoftwareKeyboardController.current
        val focusManager = LocalFocusManager.current

        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 7.dp),
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(text = stringResource(R.string.add_screen_tag_field_hint)) },
            label = { Text(stringResource(R.string.add_screen_add_tags)) },
            singleLine = true,
            trailingIcon = {
                if (value.isNotEmpty()) {
                    Row {
                        RaysIconButton(
                            onClick = {
                                onAddClick()
                                onValueChange("")
                            },
                            imageVector = Icons.Default.AddBox,
                            contentDescription = stringResource(R.string.add_screen_add_tag),
                        )
                        RaysIconButton(
                            onClick = onAddToAllClick,
                            imageVector = Icons.Default.AddToPhotos,
                            contentDescription = stringResource(R.string.add_screen_add_tag_to_all),
                        )
                        RaysIconButton(
                            onClick = { onValueChange("") },
                            imageVector = Icons.Default.Cancel,
                            contentDescription = stringResource(R.string.clear_input_text),
                        )
                    }
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                if (value.isNotBlank()) {
                    onAddClick()
                } else {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                }
                onValueChange("")
            })
        )
    }
}

@Composable
private fun AddToAllList(list: List<String>, onDeleteTag: (String) -> Unit) {
    val scope = rememberCoroutineScope()

    AnimatedVisibility(
        visible = list.isNotEmpty(),
        enter = expandVertically(),
        exit = shrinkVertically(),
    ) {
        var expandFlowRow by rememberSaveable { mutableStateOf(false) }
        val interactionSource = remember { MutableInteractionSource() }

        Card(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 7.dp),
            onClick = { expandFlowRow = !expandFlowRow },
            interactionSource = interactionSource
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    modifier = Modifier.padding(start = 16.dp),
                    text = stringResource(R.string.add_screen_global_tags),
                    style = MaterialTheme.typography.titleMedium,
                )
                val tooltipState = rememberTooltipState(isPersistent = true)
                TooltipBox(
                    positionProvider = TooltipDefaults.rememberRichTooltipPositionProvider(),
                    tooltip = {
                        RichTooltip(
                            title = { Text(stringResource(R.string.add_screen_global_tags)) },
                            text = { Text(stringResource(R.string.add_screen_global_tags_tips)) }
                        )
                    },
                    state = tooltipState,
                ) {
                    RaysIconButton(
                        onClick = { scope.launch { tooltipState.show(MutatePriority.PreventUserInput) } },
                        imageVector = Icons.AutoMirrored.Default.Help,
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                RaysIconButton(
                    onClick = { expandFlowRow = !expandFlowRow },
                    imageVector = if (expandFlowRow) Icons.Default.ExpandLess
                    else Icons.Default.ExpandMore,
                    interactionSource = interactionSource,
                )
            }
            AnimatedVisibility(
                visible = expandFlowRow,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                FlowRow(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 7.dp),
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    list.forEach { tag ->
                        InputChip(
                            selected = false,
                            label = { Text(tag) },
                            onClick = { onDeleteTag(tag) },
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
    }
}

@Composable
private fun WaitingRow(
    uris: List<UriWithStickerUuidBean>,
    onSelectStickersClick: () -> Unit,
    onSelectFirstStickerClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .padding(vertical = 7.dp)
            .fillMaxWidth()
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 16.dp),
            text = stringResource(R.string.add_screen_waiting_uris),
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(modifier = Modifier.height(10.dp))
        if (uris.isEmpty()) {
            TextButton(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                onClick = onSelectStickersClick,
            ) {
                Text(text = stringResource(R.string.add_screen_select_sticker))
            }
        } else {
            LazyRow(
                modifier = Modifier.animateContentSize(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 3.dp),
            ) {
                itemsIndexed(uris) { index, uri ->
                    val content: @Composable ColumnScope.() -> Unit = {
                        RaysImage(
                            model = uri.uri,
                            modifier = Modifier
                                .height(if (index == 0) 150.dp else 100.dp)
                                .aspectRatio(1f)
                                .run {
                                    if (index == 0) clickable(onClick = onSelectFirstStickerClick)
                                    else this
                                },
                            contentScale = ContentScale.Crop,
                        )
                    }
                    ElevatedCard(content = content)
                }
            }
        }
    }
}

@Composable
private fun AddedTags(tags: List<String>, onClick: (Int) -> Unit) {
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 7.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        repeat(tags.size) { index ->
            InputChip(
                selected = false,
                label = { Text(tags[index]) },
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
        Card(modifier = Modifier.padding(horizontal = 16.dp, vertical = 7.dp)) {
            Text(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 12.dp),
                text = stringResource(R.string.add_screen_suggest_tag),
                style = MaterialTheme.typography.titleMedium
            )
            FlowRow(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 3.dp)
                    .fillMaxWidth()
                    .heightIn(max = 100.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                repeat(suggestedTags.size) { index ->
                    SuggestionChip(
                        modifier = Modifier.combinedClickable { },
                        label = { Text(suggestedTags[index]) },
                        onClick = { onClick(index) },
                    )
                }
            }
        }
    }
}