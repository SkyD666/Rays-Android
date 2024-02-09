package com.skyd.rays.ui.screen.settings.api.apigrant

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.skyd.rays.R
import com.skyd.rays.base.mvi.getDispatcher
import com.skyd.rays.ext.showSnackbarWithLaunchedEffect
import com.skyd.rays.model.bean.ApiGrantPackageBean
import com.skyd.rays.model.preference.ApiGrantPreference
import com.skyd.rays.ui.component.BannerItem
import com.skyd.rays.ui.component.LocalUseColorfulIcon
import com.skyd.rays.ui.component.RaysIconButton
import com.skyd.rays.ui.component.RaysSwipeToDismiss
import com.skyd.rays.ui.component.RaysTopBar
import com.skyd.rays.ui.component.RaysTopBarStyle
import com.skyd.rays.ui.component.SwitchSettingsItem
import com.skyd.rays.ui.component.dialog.DeleteWarningDialog
import com.skyd.rays.ui.component.dialog.TextFieldDialog
import com.skyd.rays.ui.local.LocalApiGrant


const val API_GRANT_SCREEN_ROUTE = "apiGrantScreen"

@Composable
fun ApiGrantScreen(viewModel: ApiGrantViewModel = hiltViewModel()) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var openAddDialog by rememberSaveable { mutableStateOf(false) }
    var openDeleteDialog by rememberSaveable { mutableStateOf<String?>(null) }
    var inputPackageName by rememberSaveable { mutableStateOf("") }
    val uiState by viewModel.viewState.collectAsStateWithLifecycle()
    val uiEvent by viewModel.singleEvent.collectAsStateWithLifecycle(initialValue = null)

    val dispatch = viewModel.getDispatcher(startWith = ApiGrantIntent.GetAllApiGrant)

    when (val event = uiEvent) {
        is ApiGrantEvent.AddPackageName.Failed -> {
            snackbarHostState.showSnackbarWithLaunchedEffect(
                message = context.getString(R.string.failed_info, event.msg),
                key2 = event,
            )
        }

        ApiGrantEvent.AddPackageName.Success,
        null -> Unit
    }

    Scaffold(
        topBar = {
            RaysTopBar(
                style = RaysTopBarStyle.Large,
                scrollBehavior = scrollBehavior,
                title = { Text(text = stringResource(R.string.api_grant_screen_name)) },
                actions = {
                    RaysIconButton(
                        onClick = { openAddDialog = true },
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.uri_string_share_screen_add_app_package)
                    )
                },
            )
        }
    ) { paddingValues ->
        val apiGrant = LocalApiGrant.current
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = paddingValues,
        ) {
            item {
                BannerItem {
                    SwitchSettingsItem(
                        icon = if (apiGrant) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        text = stringResource(id = R.string.api_grant_screen_enable),
                        checked = apiGrant,
                        onCheckedChange = {
                            ApiGrantPreference.put(
                                context = context,
                                scope = scope,
                                value = it
                            )
                        }
                    )
                }
            }
            val uriStringShareResultState = uiState.apiGrantResultState
            if (uriStringShareResultState is ApiGrantResultState.Success) {
                itemsIndexed(uriStringShareResultState.data) { _, item ->
                    CompositionLocalProvider(LocalUseColorfulIcon provides true) {
                        RaysSwipeToDismiss(
                            state = rememberSwipeToDismissBoxState(
                                confirmValueChange = { dismissValue ->
                                    if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                                        openDeleteDialog = item.apiGrantPackageBean.packageName
                                    }
                                    false
                                }
                            ),
                            enableDismissFromStartToEnd = false,
                            enableDismissFromEndToStart = true,
                        ) {
                            SwitchSettingsItem(
                                modifier = Modifier.background(MaterialTheme.colorScheme.background),
                                icon = rememberDrawablePainter(drawable = item.appIcon),
                                checked = item.apiGrantPackageBean.enabled,
                                enabled = apiGrant,
                                text = item.appName,
                                description = item.apiGrantPackageBean.packageName,
                                onCheckedChange = {
                                    dispatch(
                                        ApiGrantIntent.UpdateApiGrant(
                                            item.apiGrantPackageBean.copy(enabled = it)
                                        )
                                    )
                                },
                            )
                        }
                    }
                }
            }
        }

        TextFieldDialog(
            visible = openAddDialog,
            title = stringResource(id = R.string.uri_string_share_screen_add_app_package),
            maxLines = 1,
            placeholder = stringResource(id = R.string.uri_string_share_screen_add_app_package_example),
            onDismissRequest = { openAddDialog = false },
            value = inputPackageName,
            onValueChange = { inputPackageName = it },
            onConfirm = {
                dispatch(
                    ApiGrantIntent.UpdateApiGrant(
                        ApiGrantPackageBean(packageName = it, enabled = true)
                    )
                )
                inputPackageName = ""
                openAddDialog = false
            }
        )

        DeleteWarningDialog(
            visible = openDeleteDialog != null,
            onDismissRequest = { openDeleteDialog = null },
            onDismiss = { openDeleteDialog = null },
            onConfirm = {
                dispatch(ApiGrantIntent.DeleteApiGrant(openDeleteDialog!!))
                openDeleteDialog = null
            }
        )
    }
}
