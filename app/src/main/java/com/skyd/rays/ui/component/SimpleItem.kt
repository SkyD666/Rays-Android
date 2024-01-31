package com.skyd.rays.ui.component

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

@Composable
fun RadioTextItem(
    text: String,
    selected: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        Modifier
            .clip(RoundedCornerShape(12.dp))
            .fillMaxWidth()
            .height(56.dp)
            .selectable(
                selected = selected,
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                enabled = enabled,
                role = Role.RadioButton,
                onClick = onClick,
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null,     // null recommended for accessibility with screen readers
            enabled = enabled,
            interactionSource = interactionSource,
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}