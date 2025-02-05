package com.skyd.rays.ui.screen.add

import android.os.Bundle
import android.util.Log
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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Help
import androidx.compose.material.icons.outlined.AddBox
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.AddToPhotos
import androidx.compose.material.icons.outlined.Autorenew
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.EditOff
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.HighlightOff
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import com.skyd.rays.R
import com.skyd.rays.base.mvi.MviEventListener
import com.skyd.rays.base.mvi.getDispatcher
import com.skyd.rays.ext.navigate
import com.skyd.rays.ext.plus
import com.skyd.rays.ext.popBackStackWithLifecycle
import com.skyd.rays.ext.showSnackbar
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
import com.skyd.rays.ui.screen.detail.openDetailScreen
import com.skyd.rays.ui.screen.fullimage.openFullImageScreen
import com.skyd.rays.ui.screen.search.SearchResultItem
import com.skyd.rays.util.launchImagePicker
import com.skyd.rays.util.rememberImagePicker
import kotlinx.coroutines.launch
import java.util.UUID

const val ADD_SCREEN_ROUTE = "addScreen"

fun openAddScreen(
    navController: NavHostController,
    stickers: List<UriWithStickerUuidBean>,
    isEdit: Boolean,
    navOptions: NavOptions? = null,
) {
    navController.navigate(
        ADD_SCREEN_ROUTE,
        Bundle().apply {
            putBoolean("isEdit", isEdit)
            putParcelableArrayList("stickers", ArrayList(stickers))
        },
        navOptions = navOptions,
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
    var openMoreMenu by rememberSaveable { mutableStateOf(false) }
    var openErrorDialog by rememberSaveable { mutableStateOf<String?>(null) }
    var saveButtonEnable by rememberSaveable { mutableStateOf(true) }
    val uiState by viewModel.viewState.collectAsStateWithLifecycle()
    val dispatch = viewModel.getDispatcher(startWith = AddIntent.Init(initStickers))

    LaunchedEffect(uiState.currentSticker) {
        saveButtonEnable = true
    }

    fun processNext() {
        if (uiState.waitingList.size <= 1 && isEdit) {
            navController.popBackStackWithLifecycle()
        } else {
            dispatch(
                AddIntent.RemoveWaitingListSingleSticker(
                    index = 0, onSticker = { uiState.waitingList.getOrNull(it) }
                )
            )
        }
    }

    var currentReplaceIndex = rememberSaveable { 0 }
    val replaceStickerLauncher = rememberImagePicker(multiple = false) { result ->
        if (result.firstOrNull() != null) {
            val waitingList = uiState.waitingList
            if (waitingList.isNotEmpty()) {
                dispatch(
                    AddIntent.ReplaceWaitingListSingleSticker(
                        sticker = waitingList[currentReplaceIndex].copy(uri = result.first()),
                        index = currentReplaceIndex,
                    )
                )
            }
        }
    }
    val pickStickersLauncher = rememberImagePicker(multiple = true) { result ->
        if (result.isEmpty()) return@rememberImagePicker
        dispatch(
            AddIntent.AddToWaitingList(
                result.map { UriWithStickerUuidBean(uri = it) },
                currentListIsEmpty = uiState.waitingList.isEmpty(),
            )
        )
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
                        onClick = { processNext() },
                        contentDescription = stringResource(R.string.add_screen_skip_current_sticker),
                        imageVector = Icons.Outlined.EditOff,
                    )
                    RaysIconButton(
                        onClick = {
                            if (uiState.currentSticker == null) {
                                snackbarHostState.showSnackbar(
                                    scope = scope,
                                    message = context.getString(R.string.add_screen_sticker_is_not_set),
                                )
                            } else {
                                saveButtonEnable = false
                                keyboardController?.hide()
                                focusManager.clearFocus()
                                val getStickersWithTagsState = uiState.getStickersWithTagsState
                                val stickerBean =
                                    if (getStickersWithTagsState is GetStickersWithTagsState.Success) {
                                        getStickersWithTagsState.stickerWithTags.sticker.copy(
                                            title = uiState.titleText
                                        )
                                    } else StickerBean(
                                        title = uiState.titleText,
                                        createTime = System.currentTimeMillis(),
                                        uuid = uiState.currentSticker?.stickerUuid.orEmpty()
                                            .ifBlank { UUID.randomUUID().toString() }
                                    )
                                val stickerWithTags = StickerWithTags(
                                    sticker = stickerBean,
                                    tags = (uiState.addedTags + uiState.addToAllTags).distinct()
                                        .map { TagBean(tag = it) }
                                )
                                dispatch(
                                    AddIntent.AddNewStickerWithTags(
                                        stickerWithTags,
                                        uiState.currentSticker!!.uri!!
                                    )
                                )
                            }
                        },
                        enabled = saveButtonEnable && !uiState.loadingDialog,
                        contentDescription = stringResource(R.string.add_screen_save_current_sticker),
                        imageVector = Icons.Outlined.Save,
                    )
//                    RaysIconButton(
//                        onClick = { openMoreMenu = true },
//                        contentDescription = stringResource(R.string.more),
//                        imageVector = Icons.Outlined.MoreVert,
//                    )
//                    MoreMenu(
//                        expanded = openMoreMenu,
//                        onDismissRequest = { openMoreMenu = false },
//                        onSaveAllClick = {},
//                    )
                }
            )
        },
    ) { paddingValues ->
        var openDuplicateDialog by remember { mutableStateOf(false) }

        LazyColumn(contentPadding = paddingValues) {
            item {
                Column {
                    WaitingRow(
                        uris = uiState.waitingList,
                        isEdit = isEdit,
                        onSelectStickersClick = { pickStickersLauncher.launchImagePicker() },
                        onReplaceStickerClick = { index ->
                            currentReplaceIndex = index
                            replaceStickerLauncher.launchImagePicker()
                        },
                        onRemoveStickerFromWaitingListClick = { index ->
                            dispatch(
                                AddIntent.RemoveWaitingListSingleSticker(
                                    index = index,
                                    onSticker = { uiState.waitingList.getOrNull(it) })
                            )
                        },
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
                value = uiState.titleText,
                onValueChange = { dispatch(AddIntent.UpdateTitleText(it)) },
            )
            tagsInputFieldItem(
                value = uiState.currentTagText,
                onValueChange = { dispatch(AddIntent.UpdateCurrentTagText(it)) },
                onAddClick = { dispatch(AddIntent.AddTag(uiState.currentTagText)) },
                onAddToAllClick = { dispatch(AddIntent.AddAddToAllTag(uiState.currentTagText)) }
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
                    dispatch(AddIntent.UpdateCurrentTagText(uiState.suggestTags[index]))
                    dispatch(AddIntent.AddTag(uiState.suggestTags[index]))
                    dispatch(AddIntent.RemoveSuggestTag(uiState.suggestTags[index]))
                })
            }
            item {
                SimilarStickers(similarStickers = uiState.similarStickers)
            }
        }

        MviEventListener(viewModel.singleEvent) { event ->
            when (event) {
                is AddEvent.AddStickersResultEvent.Duplicate -> {
                    saveButtonEnable = true
                    openDuplicateDialog = true
                }

                is AddEvent.AddStickersResultEvent.Failed -> {
                    saveButtonEnable = true
                    snackbarHostState.showSnackbar(
                        context.getString(R.string.failed_info, event.msg)
                    )
                }

                is AddEvent.AddStickersResultEvent.Success -> {
                    Log.w("AddScreen", "Fuck MviEventListener(viewModel.singleEvent) processNext")
                    processNext()
                }

                is AddEvent.InitFailed -> openErrorDialog = event.msg
            }
        }

        RaysDialog(
            visible = !openErrorDialog.isNullOrBlank(),
            title = { Text(text = stringResource(R.string.dialog_warning)) },
            text = { Text(text = stringResource(R.string.failed_info, openErrorDialog.orEmpty())) },
            confirmButton = {
                TextButton(onClick = {
                    openErrorDialog = null
                    navController.popBackStackWithLifecycle()
                }) {
                    Text(text = stringResource(id = R.string.dialog_exit))
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
                        imageVector = Icons.Outlined.Cancel,
                        contentDescription = stringResource(R.string.cancel),
                    )
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = {
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
                            imageVector = Icons.Outlined.AddBox,
                            contentDescription = stringResource(R.string.add_screen_add_tag),
                        )
                        RaysIconButton(
                            onClick = onAddToAllClick,
                            imageVector = Icons.Outlined.AddToPhotos,
                            contentDescription = stringResource(R.string.add_screen_add_tag_to_all),
                        )
                        RaysIconButton(
                            onClick = { onValueChange("") },
                            imageVector = Icons.Outlined.Cancel,
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
                    positionProvider = TooltipDefaults.rememberTooltipPositionProvider(),
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
                        imageVector = Icons.AutoMirrored.Outlined.Help,
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                RaysIconButton(
                    onClick = { expandFlowRow = !expandFlowRow },
                    imageVector = if (expandFlowRow) Icons.Outlined.ExpandLess
                    else Icons.Outlined.ExpandMore,
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
                                    imageVector = Icons.Outlined.Close,
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
    isEdit: Boolean,
    onSelectStickersClick: () -> Unit,
    onReplaceStickerClick: (Int) -> Unit,
    onRemoveStickerFromWaitingListClick: (Int) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 3.dp)
            .animateContentSize()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                modifier = Modifier
                    .height(40.dp)
                    .padding(start = 16.dp)
                    .wrapContentHeight(align = Alignment.CenterVertically),
                text = stringResource(R.string.add_screen_waiting_uris),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
            )
            if (!isEdit) {
                RaysIconButton(
                    onClick = onSelectStickersClick,
                    imageVector = Icons.Outlined.AddPhotoAlternate,
                    contentDescription = stringResource(R.string.add_screen_add_stickers),
                )
            }
        }
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
                itemsIndexed(items = uris) { index, uri ->
                    ElevatedCard {
                        val navController = LocalNavController.current
                        Box(
                            modifier = Modifier.clickable {
                                openFullImageScreen(
                                    navController = navController,
                                    image = uri.uri!!
                                )
                            },
                            contentAlignment = Alignment.TopEnd,
                        ) {
                            RaysImage(
                                model = uri.uri,
                                blur = false,
                                modifier = Modifier
                                    .height(if (index == 0) 150.dp else 100.dp)
                                    .widthIn(max = 170.dp)
                                    .align(Alignment.Center),
                                contentScale = ContentScale.Fit,
                            )
                            Row(modifier = Modifier.padding(3.dp)) {
                                val iconButtonModifier = Modifier.size(36.dp)
                                val iconButtonColors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = Color.Black.copy(alpha = 0.3f),
                                    contentColor = Color.White,
                                    disabledContainerColor = Color.Black.copy(alpha = 0.2f),
                                    disabledContentColor = Color.White,
                                )
                                RaysIconButton(
                                    modifier = iconButtonModifier,
                                    colors = iconButtonColors,
                                    onClick = { onRemoveStickerFromWaitingListClick(index) },
                                    imageVector = Icons.Outlined.HighlightOff,
                                    contentDescription = stringResource(R.string.add_screen_remove_sticker_from_waiting_list),
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                RaysIconButton(
                                    modifier = iconButtonModifier,
                                    colors = iconButtonColors,
                                    onClick = { onReplaceStickerClick(index) },
                                    imageVector = Icons.Outlined.Autorenew,
                                    contentDescription = stringResource(R.string.add_screen_update_sticker),
                                )
                            }
                        }
                    }
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
                        imageVector = Icons.Outlined.Close,
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
                style = MaterialTheme.typography.titleMedium,
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

@Composable
private fun SimilarStickers(similarStickers: List<StickerWithTags>) {
    AnimatedVisibility(
        visible = similarStickers.isNotEmpty(),
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
    ) {
        val navController = LocalNavController.current
        Card(modifier = Modifier.padding(horizontal = 16.dp, vertical = 7.dp)) {
            Text(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 12.dp),
                text = stringResource(R.string.add_screen_similar_stickers),
                style = MaterialTheme.typography.titleMedium,
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 12.dp) +
                        PaddingValues(bottom = 12.dp, top = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                items(similarStickers) { sticker ->
                    SearchResultItem(
                        modifier = Modifier
                            .height(70.dp)
                            .width(IntrinsicSize.Max)
                            .widthIn(max = 120.dp),
                        data = sticker,
                        selectable = false,
                        selected = false,
                        contentScale = ContentScale.FillHeight,
                        imageAspectRatio = null,
                        onClick = {
                            openDetailScreen(
                                navController = navController,
                                stickerUuid = it.sticker.uuid
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun MoreMenu(expanded: Boolean, onDismissRequest: () -> Unit, onSaveAllClick: () -> Unit) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest
    ) {
        DropdownMenuItem(
            text = { Text(stringResource(R.string.add_screen_save_all)) },
            onClick = {
                onDismissRequest()
                onSaveAllClick()
            },
        )
    }
}