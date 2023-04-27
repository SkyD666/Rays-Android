package com.skyd.rays.ui.screen.about.update

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skyd.rays.R
import com.skyd.rays.base.LoadUiIntent
import com.skyd.rays.model.bean.UpdateBean
import com.skyd.rays.model.preference.IgnoreUpdateVersionPreference
import com.skyd.rays.ui.component.dialog.RaysDialog
import com.skyd.rays.ui.component.dialog.WaitingDialog
import com.skyd.rays.ui.local.LocalIgnoreUpdateVersion
import okhttp3.internal.toLongOrDefault


@Composable
fun UpdateDialog(
    silence: Boolean = false,
    onClosed: () -> Unit = {},
    viewModel: UpdateViewModel = hiltViewModel()
) {
    var openWaitingDialog by rememberSaveable { mutableStateOf(false) }
    val uiStateFlow by viewModel.uiStateFlow.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.sendUiIntent(UpdateIntent.CheckUpdate)
    }

    WaitingDialog(visible = openWaitingDialog && !silence)

    viewModel.loadUiIntentFlow.collectAsStateWithLifecycle(initialValue = null).value?.also {
        when (it) {
            is LoadUiIntent.Error -> {}

            is LoadUiIntent.Loading -> {
                openWaitingDialog = it.isShow
            }

            LoadUiIntent.ShowMainView -> {}
        }
    }

    when (val updateUiState = uiStateFlow.updateUiState) {
        UpdateUiState.Init -> {}
        is UpdateUiState.OpenNewerDialog -> {
            NewerDialog(
                updateBean = updateUiState.data,
                silence = silence,
                onDismissRequest = {
                    onClosed()
                    viewModel.sendUiIntent(UpdateIntent.CloseDialog)
                }
            )
        }

        UpdateUiState.OpenNoUpdateDialog -> {
            NoUpdateDialog(
                visible = !silence,
                onDismissRequest = {
                    onClosed()
                    viewModel.sendUiIntent(UpdateIntent.CloseDialog)
                }
            )
        }
    }
}

@Composable
private fun NewerDialog(
    updateBean: UpdateBean?,
    silence: Boolean,
    onDismissRequest: () -> Unit,
    viewModel: UpdateViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val ignoreUpdateVersion = LocalIgnoreUpdateVersion.current
    val scope = rememberCoroutineScope()

    val visible = updateBean != null &&
            (!silence || ignoreUpdateVersion < updateBean.tagName.toLongOrDefault(0L))

    if (!visible) {
        onDismissRequest()
    }

    RaysDialog(
        onDismissRequest = onDismissRequest,
        visible = visible,
        title = {
            Text(text = stringResource(R.string.update_newer))
        },
        text = {
            Column {
                SelectionContainer(modifier = Modifier.weight(weight = 1f, fill = false)) {
                    Text(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        text = stringResource(
                            R.string.update_newer_text,
                            updateBean!!.name,
                            updateBean.publishedAt,
                            updateBean.assets.firstOrNull()?.downloadCount.toString(),
                            updateBean.body,
                        )
                    )
                }
                val checked = ignoreUpdateVersion == updateBean!!.tagName.toLongOrDefault(0L)
                Spacer(modifier = Modifier.height(5.dp))
                Card(colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .toggleable(
                                value = checked,
                                onValueChange = {
                                    IgnoreUpdateVersionPreference.put(
                                        context = context,
                                        scope = scope,
                                        value = if (it) {
                                            onDismissRequest()
                                            updateBean.tagName.toLongOrDefault(0L)
                                        } else {
                                            0L
                                        }
                                    )
                                },
                                role = Role.Checkbox
                            )
                            .padding(horizontal = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = checked,
                            onCheckedChange = null
                        )
                        Text(
                            modifier = Modifier
                                .padding(start = 16.dp)
                                .padding(vertical = 6.dp),
                            text = stringResource(R.string.update_ignore),
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    viewModel.sendUiIntent(
                        UpdateIntent.Update(
                            updateBean?.assets?.firstOrNull()?.browserDownloadUrl
                        )
                    )
                }
            ) {
                Text(text = stringResource(id = R.string.download_update))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequest
            ) {
                Text(text = stringResource(id = R.string.dialog_cancel))
            }
        }
    )
}

@Composable
private fun NoUpdateDialog(
    visible: Boolean,
    onDismissRequest: () -> Unit,
) {
    if (!visible) {
        onDismissRequest()
    }

    RaysDialog(
        onDismissRequest = onDismissRequest,
        visible = visible,
        title = {
            Text(text = stringResource(R.string.update_check))
        },
        text = {
            Text(text = stringResource(R.string.update_no_update))
        },
        confirmButton = {
            TextButton(
                onClick = onDismissRequest
            ) {
                Text(text = stringResource(id = R.string.dialog_ok))
            }
        }
    )
}