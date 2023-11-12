package com.skyd.rays.ui.screen.add

import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AddToPhotos
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CancelScheduleSend
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.EditOff
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.skyd.rays.R
import com.skyd.rays.appContext
import com.skyd.rays.base.LoadUiIntent
import com.skyd.rays.config.refreshStickerData
import com.skyd.rays.ext.addAllDistinctly
import com.skyd.rays.ext.addIfAny
import com.skyd.rays.ext.navigate
import com.skyd.rays.ext.plus
import com.skyd.rays.ext.popBackStackWithLifecycle
import com.skyd.rays.ext.showSnackbar
import com.skyd.rays.ext.showSnackbarWithLaunchedEffect
import com.skyd.rays.model.bean.StickerBean
import com.skyd.rays.model.bean.StickerWithTags
import com.skyd.rays.model.bean.TagBean
import com.skyd.rays.model.bean.UriWithStickerUuidBean
import com.skyd.rays.ui.component.AnimatedPlaceholder
import com.skyd.rays.ui.component.RaysCard
import com.skyd.rays.ui.component.RaysFloatingActionButton
import com.skyd.rays.ui.component.RaysFloatingActionButtonStyle
import com.skyd.rays.ui.component.RaysIconButton
import com.skyd.rays.ui.component.RaysImage
import com.skyd.rays.ui.component.RaysTopBar
import com.skyd.rays.ui.component.dialog.RaysDialog
import com.skyd.rays.ui.local.LocalNavController
import com.skyd.rays.util.stickerUuidToUri
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
    var openDialog by remember { mutableStateOf(false) }
    var openDuplicateDialog by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val navController = LocalNavController.current
    var titleText by rememberSaveable { mutableStateOf("") }
    var currentTagText by rememberSaveable { mutableStateOf("") }
    var stickerCreateTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val tagsToAllWaitingStickers = remember { mutableStateListOf<TagBean>() }
    val tags = remember { mutableStateListOf<TagBean>() }
    val stickersWaitingList = remember { mutableStateListOf<UriWithStickerUuidBean>() }
    val suggestedTags = remember { mutableStateListOf<String>() }
    val loadUiIntent by viewModel.loadUiIntentFlow.collectAsStateWithLifecycle(initialValue = null)
    val uiEvent by viewModel.uiEventFlow.collectAsStateWithLifecycle(initialValue = null)

    fun currentSticker(): UriWithStickerUuidBean? = stickersWaitingList.firstOrNull()

    // 添加/修改完成后重设页面数据
    fun resetStickerData() {
        titleText = ""
        currentTagText = ""
        stickerCreateTime = System.currentTimeMillis()
        suggestedTags.clear()
        tags.clear()
        tags.addAll(tagsToAllWaitingStickers)
        if (stickersWaitingList.isNotEmpty()) {
            stickersWaitingList.removeAt(0)
        }
    }

    LaunchedEffect(Unit) {
        stickersWaitingList.addAll(initStickers)
        currentSticker()?.let { currentSticker ->
            if (currentSticker.stickerUuid.isNotBlank()) {
                viewModel.sendUiIntent(AddIntent.GetStickerWithTags(currentSticker.stickerUuid))
            }
        }
    }

    LaunchedEffect(currentSticker()) {
        val currentSticker = currentSticker()
        if (currentSticker != null) {
            currentSticker.uri?.let { uri ->
                viewModel.sendUiIntent(AddIntent.GetSuggestTags(uri))
            }
        } else {
            resetStickerData()
            if (isEdit) {
                navController.popBackStackWithLifecycle()
            }
        }
    }

    val pickStickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { result ->
        if (result != null) stickersWaitingList[0] = UriWithStickerUuidBean(uri = result)
    }
    val pickStickersLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { result ->
        if (result.isEmpty()) return@rememberLauncherForActivityResult
        stickersWaitingList.addAllDistinctly(result.map { UriWithStickerUuidBean(uri = it) })
    }

    Scaffold(
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
                        contentDescription = stringResource(R.string.add_screen_select_sticker),
                        imageVector = if (isEdit) Icons.Default.Image else Icons.Default.AddPhotoAlternate,
                    )
                    RaysIconButton(
                        onClick = { resetStickerData() },
                        contentDescription = stringResource(R.string.add_screen_skip_current_sticker),
                        imageVector = Icons.Default.EditOff,
                    )
                    RaysIconButton(
                        onClick = {
                            if (currentSticker() == null) {
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
                                    ).apply { uuid = currentSticker()!!.stickerUuid },
                                    tags = tags.distinct()
                                )
                                viewModel.sendUiIntent(
                                    AddIntent.AddNewStickerWithTags(
                                        stickerWithTags,
                                        currentSticker()!!.uri!!
                                    )
                                )
                            }
                        },
                        contentDescription = stringResource(R.string.add_screen_save),
                        imageVector = Icons.Default.Save,
                    )
                }
            )
        },

        ) { paddingValues ->
        LazyColumn(contentPadding = paddingValues) {
            item {
                AnimatedVisibility(
                    visible = stickersWaitingList.isNotEmpty(),
                    enter = expandVertically(),
                    exit = shrinkVertically(),
                ) {
                    Column {
                        WaitingRow(
                            uris = stickersWaitingList,
                            onStickerDeleted = { stickersWaitingList.remove(it) },
                        )
                        AddToAllList(
                            list = tagsToAllWaitingStickers,
                            onDeleteTag = { tagsToAllWaitingStickers.remove(it) }
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
                onAddClick = {
                    tags.addIfAny(TagBean(tag = currentTagText)) { it.tag != currentTagText }
                },
                onAddToAllClick = {
                    tagsToAllWaitingStickers.addIfAny(TagBean(tag = currentTagText)) {
                        it.tag != currentTagText
                    }
                }
            )
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
        }

        RaysDialog(
            visible = openDialog,
            title = { Text(text = stringResource(R.string.info)) },
            text = { Text(text = stringResource(R.string.add_screen_success)) },
            onDismissRequest = {
                openDialog = false
                if (stickersWaitingList.isEmpty()) {
                    navController.popBackStackWithLifecycle()
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    openDialog = false
                    if (stickersWaitingList.isEmpty()) {
                        navController.popBackStackWithLifecycle()
                    }
                }) {
                    Text(text = stringResource(id = R.string.dialog_ok))
                }
            }
        )

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
    }

    uiEvent?.apply {
        when (getStickersWithTagsUiEvent) {
            is GetStickersWithTagsUiEvent.Success -> {
                val stickerBean = getStickersWithTagsUiEvent.stickerWithTags.sticker
                val stickerIndex = stickersWaitingList
                    .indexOfFirst { it.stickerUuid == stickerBean.uuid }
                if (stickerIndex > -1) {
                    stickersWaitingList[stickerIndex] = UriWithStickerUuidBean(
                        uri = stickerUuidToUri(stickerBean.uuid),
                        stickerUuid = stickerBean.uuid
                    )
                }
                titleText = stickerBean.title
                stickerCreateTime = stickerBean.createTime
                tags.clear()
                tags.addAll(getStickersWithTagsUiEvent.stickerWithTags.tags.distinct())
            }

            GetStickersWithTagsUiEvent.Init,
            GetStickersWithTagsUiEvent.Failed,
            null -> Unit
        }

        when (addStickersResultUiEvent) {
            AddStickersResultUiEvent.Duplicate -> openDuplicateDialog = true

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
                snackbarHostState.showSnackbarWithLaunchedEffect(
                    context.getString(R.string.add_screen_error, it.msg),
                    key2 = it,
                )
            }

            is LoadUiIntent.Loading -> Unit
        }
    }
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
                            onClick = onAddToAllClick,
                            imageVector = Icons.Default.AddToPhotos,
                            contentDescription = stringResource(R.string.add_screen_add_tag_to_all),
                        )
                        RaysIconButton(
                            onClick = { onValueChange("") },
                            imageVector = Icons.Default.Cancel,
                            contentDescription = stringResource(R.string.cancel),
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
private fun AddToAllList(list: List<TagBean>, onDeleteTag: (TagBean) -> Unit) {
    val scope = rememberCoroutineScope()

    AnimatedVisibility(
        visible = list.isNotEmpty(),
        enter = expandVertically(),
        exit = shrinkVertically(),
    ) {
        var expandFlowRow by rememberSaveable { mutableStateOf(false) }
        val interactionSource = remember { MutableInteractionSource() }

        RaysCard(
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
                            label = { Text(tag.tag) },
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
    onStickerDeleted: (UriWithStickerUuidBean) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 7.dp)) {
        Text(
            modifier = Modifier.padding(horizontal = 16.dp),
            text = stringResource(R.string.add_screen_waiting_uris),
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(modifier = Modifier.height(10.dp))
        LazyRow(
            modifier = Modifier.height(100.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
        ) {
            items(uris) { uri ->
                RaysCard(
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    onClick = { onStickerDeleted(uri) }
                ) {
                    RaysImage(
                        model = uri.uri,
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
private fun AddedTags(tags: List<TagBean>, onClick: (Int) -> Unit) {
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
        RaysCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 7.dp)) {
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