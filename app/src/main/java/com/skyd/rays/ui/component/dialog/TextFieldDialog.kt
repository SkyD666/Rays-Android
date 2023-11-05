/**
 * Copyright (C) 2023 Ashinch
 *
 * @link https://github.com/Ashinch/ReadYou
 * @author Ashinch
 * @modifier SkyD666
 */
package com.skyd.rays.ui.component.dialog

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import com.skyd.rays.R
import com.skyd.rays.ui.component.ClipboardTextField

@Composable
fun TextFieldDialog(
    modifier: Modifier = Modifier,
    visible: Boolean = false,
    readOnly: Boolean = false,
    maxLines: Int = Int.MAX_VALUE,
    title: String = "",
    value: String = "",
    placeholder: String = "",
    isPassword: Boolean = false,
    errorText: String = "",
    dismissText: String = stringResource(R.string.cancel),
    confirmText: String = stringResource(R.string.dialog_ok),
    onValueChange: (String) -> Unit = {},
    onDismissRequest: () -> Unit = {},
    onConfirm: (String) -> Unit = {},
    imeAction: ImeAction = if (maxLines == 1) ImeAction.Done else ImeAction.Default,
) {
    val focusManager = LocalFocusManager.current

    RaysDialog(
        modifier = modifier,
        visible = visible,
        onDismissRequest = onDismissRequest,
        title = {
            Text(text = title, maxLines = 2, overflow = TextOverflow.Ellipsis)
        },
        text = {
            ClipboardTextField(
                modifier = modifier,
                readOnly = readOnly,
                value = value,
                maxLines = maxLines,
                onValueChange = onValueChange,
                placeholder = placeholder,
                isPassword = isPassword,
                errorText = errorText,
                imeAction = imeAction,
                focusManager = focusManager,
                onConfirm = onConfirm,
            )
        },
        confirmButton = {
            TextButton(
                enabled = value.isNotBlank(),
                onClick = {
                    focusManager.clearFocus()
                    onConfirm(value)
                }
            ) {
                Text(
                    text = confirmText,
                    color = if (value.isNotBlank()) {
                        Color.Unspecified
                    } else {
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
                    }
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = dismissText)
            }
        },
    )
}
