package com.skyd.rays.ui.component.dialog

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.skyd.rays.R

@Composable
fun MultiChoiceDialog(
    modifier: Modifier = Modifier,
    visible: Boolean,
    onDismissRequest: () -> Unit = {},
    title: @Composable (() -> Unit)? = null,
    options: List<String>,
    checkedIndexList: List<Int>,
    onConfirm: (List<Int>) -> Unit,
    confirmText: String = stringResource(id = R.string.dialog_ok),
    dismissText: String? = stringResource(id = R.string.cancel),
) {
    val selectedIndexList = remember(visible) { checkedIndexList.toMutableStateList() }
    RaysDialog(
        modifier = modifier,
        visible = visible,
        onDismissRequest = onDismissRequest,
        title = title,
        text = {
            Column {
                options.forEachIndexed { index, item ->
                    MultiChoiceItem(
                        checked = index in selectedIndexList,
                        text = item,
                        onClick = {
                            if (index in selectedIndexList) {
                                selectedIndexList.remove(index)
                            } else {
                                selectedIndexList.add(index)
                            }
                        },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedIndexList) }) { Text(text = confirmText) }
        },
        dismissButton = if (dismissText == null) null else {
            { TextButton(onClick = onDismissRequest) { Text(text = dismissText) } }
        },
    )
}

@Composable
private fun MultiChoiceItem(
    checked: Boolean,
    text: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        Modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(20))
            .selectable(
                selected = checked,
                onClick = onClick,
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                role = Role.Checkbox
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            interactionSource = interactionSource,
            onCheckedChange = null // null recommended for accessibility with screen readers
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}