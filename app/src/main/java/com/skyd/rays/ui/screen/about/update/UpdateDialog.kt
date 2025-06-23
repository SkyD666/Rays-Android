package com.skyd.rays.ui.screen.about.update

import android.text.Html
import android.text.method.LinkMovementMethod
import android.widget.TextView
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skyd.compone.component.dialog.ComponeDialog
import com.skyd.compone.component.dialog.WaitingDialog
import com.skyd.rays.R
import com.skyd.rays.base.mvi.MviEventListener
import com.skyd.rays.base.mvi.getDispatcher
import com.skyd.rays.model.bean.UpdateBean
import com.skyd.rays.model.preference.IgnoreUpdateVersionPreference
import com.skyd.rays.ui.local.LocalIgnoreUpdateVersion
import okhttp3.internal.toLongOrDefault
import org.koin.compose.viewmodel.koinViewModel


@Composable
fun UpdateDialog(
    silence: Boolean = false,
    isRetry: Boolean = false,
    onSuccess: () -> Unit = {},
    onClosed: () -> Unit = {},
    onError: (String) -> Unit = {},
    viewModel: UpdateViewModel = koinViewModel()
) {
    val uiState by viewModel.viewState.collectAsStateWithLifecycle()
    val dispatch = viewModel.getDispatcher(startWith = UpdateIntent.CheckUpdate(isRetry = false))

    LaunchedEffect(Unit) {
        if (isRetry) {
            dispatch(UpdateIntent.CheckUpdate(isRetry = true))
        }
    }

    WaitingDialog(visible = uiState.loadingDialog && !silence)

    when (val updateUiState = uiState.updateUiState) {
        UpdateUiState.Init -> Unit
        is UpdateUiState.OpenNewerDialog -> {
            NewerDialog(
                updateBean = updateUiState.data,
                silence = silence,
                onDismissRequest = {
                    onClosed()
                    dispatch(UpdateIntent.CloseDialog)
                },
                onDownloadClick = { updateBean ->
                    dispatch(
                        UpdateIntent.Update(updateBean?.htmlUrl)
                    )
                }
            )
        }

        UpdateUiState.OpenNoUpdateDialog -> {
            NoUpdateDialog(
                visible = !silence,
                onDismissRequest = {
                    onClosed()
                    dispatch(UpdateIntent.CloseDialog)
                }
            )
        }
    }

    MviEventListener(viewModel.singleEvent) { event ->
        when (event) {
            is UpdateEvent.CheckError -> onError(event.msg)
            is UpdateEvent.CheckSuccess -> onSuccess()
        }
    }
}

@Composable
private fun NewerDialog(
    updateBean: UpdateBean?,
    silence: Boolean,
    onDismissRequest: () -> Unit,
    onDownloadClick: (UpdateBean?) -> Unit,
) {
    val context = LocalContext.current
    val ignoreUpdateVersion = LocalIgnoreUpdateVersion.current
    val scope = rememberCoroutineScope()

    val visible = updateBean != null &&
            (!silence || ignoreUpdateVersion < updateBean.tagName.toLongOrDefault(0L))

    if (!visible) {
        onDismissRequest()
    }

    ComponeDialog(
        onDismissRequest = onDismissRequest,
        visible = visible,
        title = {
            Text(text = stringResource(R.string.update_newer))
        },
        selectable = false,
        text = {
            Column {
                Column(modifier = Modifier.weight(weight = 1f, fill = false)) {
                    SelectionContainer {
                        Text(
                            text = stringResource(
                                R.string.update_newer_text,
                                updateBean!!.name,
                                updateBean.publishedAt,
                                updateBean.assets.firstOrNull()?.downloadCount.toString(),
                            )
                        )
                    }
                    val textColor = LocalContentColor.current
                    AndroidView(
                        factory = { context ->
                            TextView(context).apply {
                                setTextColor(textColor.toArgb())
                                setTextIsSelectable(true)
                                movementMethod = LinkMovementMethod.getInstance()
                                isSingleLine = false
                                text = Html.fromHtml(updateBean!!.body, Html.FROM_HTML_MODE_COMPACT)
                            }
                        }
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
            TextButton(onClick = { onDownloadClick(updateBean) }) {
                Text(text = stringResource(id = R.string.download_update))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = stringResource(id = R.string.cancel))
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

    ComponeDialog(
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