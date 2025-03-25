package com.skyd.rays.ui.screen.settings.data.cache

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Gif
import androidx.compose.material.icons.outlined.Image
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
import com.skyd.rays.base.mvi.MviEventListener
import com.skyd.rays.base.mvi.getDispatcher
import com.skyd.rays.ui.component.BaseSettingsItem
import com.skyd.rays.ui.component.RaysTopBar
import com.skyd.rays.ui.component.RaysTopBarStyle
import com.skyd.rays.ui.component.dialog.WaitingDialog
import kotlinx.serialization.Serializable


@Serializable
data object CacheRoute

@Composable
fun CacheScreen(viewModel: CacheViewModel = hiltViewModel()) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val uiState by viewModel.viewState.collectAsStateWithLifecycle()
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
                    painter = rememberVectorPainter(image = Icons.Outlined.Image),
                    text = stringResource(id = R.string.cache_screen_delete_provider_thumbnails),
                    descriptionText = stringResource(id = R.string.cache_screen_delete_provider_thumbnails_description),
                    onClick = { dispatch(CacheIntent.DeleteDocumentsProviderThumbnails) }
                )
            }
            item {
                BaseSettingsItem(
                    painter = rememberVectorPainter(image = Icons.Outlined.Gif),
                    text = stringResource(id = R.string.cache_screen_delete_all_mimetypes),
                    descriptionText = stringResource(id = R.string.cache_screen_delete_all_mimetypes_description),
                    onClick = { dispatch(CacheIntent.DeleteAllMimetypes) }
                )
            }
        }

        MviEventListener(viewModel.singleEvent) { event ->
            when (event) {
                is CacheEvent.DeleteDocumentsProviderThumbnailsResultEvent.Success -> snackbarHostState.showSnackbar(
                    context.getString(
                        R.string.cache_screen_delete_provider_thumbnails_success,
                        event.time / 1000.0f
                    ),
                )

                is CacheEvent.DeleteAllMimetypesResultEvent.Success -> snackbarHostState.showSnackbar(
                    context.getString(
                        R.string.cache_screen_delete_all_mimetypes_success,
                        event.time / 1000.0f
                    ),
                )
            }
        }

        WaitingDialog(visible = uiState.loadingDialog)
    }
}
