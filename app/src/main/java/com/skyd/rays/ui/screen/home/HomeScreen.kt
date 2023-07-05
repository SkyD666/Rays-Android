package com.skyd.rays.ui.screen.home

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skyd.rays.R
import com.skyd.rays.base.LoadUiIntent
import com.skyd.rays.config.refreshStickerData
import com.skyd.rays.ext.screenIsLand
import com.skyd.rays.model.bean.StickerWithTags
import com.skyd.rays.model.preference.StickerScalePreference
import com.skyd.rays.ui.component.AnimatedPlaceholder
import com.skyd.rays.ui.component.RaysIconButton
import com.skyd.rays.ui.component.RaysIconButtonStyle
import com.skyd.rays.ui.component.RaysImage
import com.skyd.rays.ui.local.LocalCurrentStickerUuid
import com.skyd.rays.ui.local.LocalHomeShareButtonAlignment
import com.skyd.rays.ui.local.LocalNavController
import com.skyd.rays.ui.local.LocalQuery
import com.skyd.rays.ui.local.LocalStickerScale
import com.skyd.rays.ui.screen.add.ADD_SCREEN_ROUTE
import com.skyd.rays.ui.screen.home.searchbar.RaysSearchBar
import com.skyd.rays.util.sendStickerByUuid
import kotlinx.coroutines.launch


@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val currentStickerUuid = LocalCurrentStickerUuid.current
    val initQuery = LocalQuery.current
    var query by rememberSaveable(initQuery) { mutableStateOf(initQuery) }
    val uiState by viewModel.uiStateFlow.collectAsStateWithLifecycle()
    val uiEvent by viewModel.uiEventFlow.collectAsStateWithLifecycle(initialValue = null)
    val loadUiIntent by viewModel.loadUiIntentFlow.collectAsStateWithLifecycle(initialValue = null)

    refreshStickerData.collectAsStateWithLifecycle(initialValue = null).apply {
        value ?: return@apply
        viewModel.sendUiIntent(HomeIntent.GetStickerWithTagsList(query))
        viewModel.sendUiIntent(HomeIntent.GetStickerDetails(currentStickerUuid))
    }

    LaunchedEffect(query) {
        viewModel.sendUiIntent(HomeIntent.GetStickerWithTagsList(query))
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        contentWindowInsets = if (context.screenIsLand) {
            WindowInsets(
                left = 0,
                top = 0,
                right = ScaffoldDefaults.contentWindowInsets
                    .getRight(LocalDensity.current, LocalLayoutDirection.current),
                bottom = 0
            )
        } else {
            WindowInsets(0.dp)
        }
    ) { innerPaddings ->
        Column(
            modifier = Modifier
                .padding(innerPaddings)
                .fillMaxSize()
        ) {
            uiState.apply {
                RaysSearchBar(
                    query = query,
                    onQueryChange = { query = it },
                    stickerWithTags = (stickerDetailUiState as? StickerDetailUiState.Success)?.stickerWithTags,
                    uiState = uiState,
                )
                when (stickerDetailUiState) {
                    is StickerDetailUiState.Init -> {
                        AnimatedPlaceholder(
                            resId = R.raw.lottie_genshin_impact_venti_1,
                            tip = stringResource(id = R.string.home_screen_empty_tip)
                        )
                        if (stickerDetailUiState.stickerUuid.isNotBlank()) {
                            viewModel.sendUiIntent(
                                HomeIntent.GetStickerDetails(stickerDetailUiState.stickerUuid)
                            )
                        }
                    }

                    is StickerDetailUiState.Success -> {
                        Spacer(modifier = Modifier.height(16.dp))
                        MainCard(stickerWithTags = stickerDetailUiState.stickerWithTags)
                    }
                }
            }
        }

        uiEvent?.apply {
            when (homeResultUiEvent) {
                is HomeResultUiEvent.Success -> {
                    LaunchedEffect(this) {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = context.getString(
                                    R.string.home_screen_export_result,
                                    homeResultUiEvent.successCount
                                ),
                                withDismissAction = true
                            )
                        }
                    }
                }

                null -> Unit
            }
        }

        loadUiIntent?.also { loadUiIntent ->
            when (loadUiIntent) {
                is LoadUiIntent.Error -> {
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = context.getString(
                                R.string.home_screen_failed, loadUiIntent.msg
                            ),
                            withDismissAction = true
                        )
                    }
                }

                is LoadUiIntent.Loading -> Unit
            }
        }
    }
}

@Composable
private fun MainCard(stickerWithTags: StickerWithTags) {
    val navController = LocalNavController.current
    val currentStickerUuid = LocalCurrentStickerUuid.current
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    val stickerBean = stickerWithTags.sticker
    val tags = stickerWithTags.tags

    Card(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp)
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .combinedClickable(
                    onLongClick = {
                        context.sendStickerByUuid(
                            uuid = stickerBean.uuid,
                            onSuccess = { stickerBean.shareCount++ }
                        )
                    },
                    onDoubleClick = {
                        navController.navigate("$ADD_SCREEN_ROUTE?stickerUuid=${currentStickerUuid}")
                    },
                    onClick = {}
                )
        ) {
            Box {
                RaysImage(
                    modifier = Modifier.fillMaxWidth(),
                    uuid = stickerBean.uuid,
                    contentScale = StickerScalePreference.toContentScale(LocalStickerScale.current),
                )
                Box(
                    modifier = Modifier.matchParentSize(),
                    contentAlignment = LocalHomeShareButtonAlignment.current
                ) {
                    RaysIconButton(
                        style = RaysIconButtonStyle.FilledTonal,
                        imageVector = Icons.Default.Share,
                        contentDescription = stringResource(R.string.home_screen_send_sticker),
                        onClick = {
                            context.sendStickerByUuid(
                                uuid = stickerBean.uuid,
                                onSuccess = { stickerBean.shareCount++ }
                            )
                        },
                    )
                }
            }
            if (stickerBean.title.isNotBlank()) {
                Text(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(top = 16.dp, bottom = if (tags.isEmpty()) 16.dp else 0.dp)
                        .basicMarquee(iterations = Int.MAX_VALUE),
                    text = stickerBean.title,
                    style = MaterialTheme.typography.titleLarge
                )
            }
            if (tags.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier
                        .padding(vertical = 6.dp, horizontal = 16.dp)
                        .fillMaxWidth()
                        .heightIn(max = 150.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    repeat(tags.size) { index ->
                        AssistChip(
                            onClick = { clipboardManager.setText(AnnotatedString(tags[index].tag)) },
                            label = { Text(tags[index].tag) }
                        )
                    }
                }
            }
        }
    }
}
