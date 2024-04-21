package com.skyd.rays.ui.screen.settings.data.cache

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Gif
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skyd.rays.R
import com.skyd.rays.base.mvi.getDispatcher
import com.skyd.rays.ext.showSnackbarWithLaunchedEffect
import com.skyd.rays.ui.component.BaseSettingsItem
import com.skyd.rays.ui.component.RaysTopBar
import com.skyd.rays.ui.component.RaysTopBarStyle
import com.skyd.rays.ui.component.dialog.WaitingDialog

const val CACHE_SCREEN_ROUTE = "cacheScreen"

@Composable
fun CacheScreen(viewModel: CacheViewModel = hiltViewModel()) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val uiState by viewModel.viewState.collectAsStateWithLifecycle()
    val uiEvent by viewModel.singleEvent.collectAsStateWithLifecycle(initialValue = null)

    val dispatch = viewModel.getDispatcher(startWith = CacheIntent.Init)

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            RaysTopBar(
                style = RaysTopBarStyle.Large,
                scrollBehavior = scrollBehavior,
                title = { Text(text = stringResource(R.string.cache_screen_name)) },
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = paddingValues,
        ) {
            item {
                BaseSettingsItem(
                    painter = rememberVectorPainter(image = Icons.Default.Image),
                    text = stringResource(id = R.string.cache_screen_delete_provider_thumbnails),
                    descriptionText = stringResource(id = R.string.cache_screen_delete_provider_thumbnails_description),
                    onClick = { dispatch(CacheIntent.DeleteDocumentsProviderThumbnails) }
                )
            }
            item {
                BaseSettingsItem(
                    painter = rememberVectorPainter(image = Icons.Default.Gif),
                    text = stringResource(id = R.string.cache_screen_delete_all_mimetypes),
                    descriptionText = stringResource(id = R.string.cache_screen_delete_all_mimetypes_description),
                    onClick = { dispatch(CacheIntent.DeleteAllMimetypes) }
                )
            }
        }

        when (val event = uiEvent) {
            is CacheEvent.DeleteDocumentsProviderThumbnailsResultEvent.Success -> {
                snackbarHostState.showSnackbarWithLaunchedEffect(
                    message = context.getString(
                        R.string.cache_screen_delete_provider_thumbnails_success,
                        event.time / 1000.0f
                    ),
                    key2 = event,
                )
            }

            is CacheEvent.DeleteAllMimetypesResultEvent.Success -> {
                snackbarHostState.showSnackbarWithLaunchedEffect(
                    message = context.getString(
                        R.string.cache_screen_delete_all_mimetypes_success,
                        event.time / 1000.0f
                    ),
                    key2 = event,
                )
            }

            null -> Unit
        }

        WaitingDialog(visible = uiState.loadingDialog)
    }
}
