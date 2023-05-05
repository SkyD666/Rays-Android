package com.skyd.rays.ui.component

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun SwitchSettingsItem(
    icon: ImageVector,
    text: String,
    description: String? = null,
    checked: Boolean = false,
    onCheckedChange: ((Boolean) -> Unit)?,
    onLongClick: (() -> Unit)? = null,
) {
    SwitchSettingsItem(
        icon = rememberVectorPainter(image = icon),
        text = text,
        description = description,
        checked = checked,
        onCheckedChange = onCheckedChange,
        onLongClick = onLongClick,
    )
}

@Composable
fun SwitchSettingsItem(
    icon: Painter,
    text: String,
    description: String? = null,
    checked: Boolean = false,
    onCheckedChange: ((Boolean) -> Unit)?,
    onLongClick: (() -> Unit)? = null,
) {
    BaseSettingsItem(
        icon = icon,
        text = text,
        descriptionText = description,
        onLongClick = onLongClick,
        onClick = {
            onCheckedChange?.invoke(!checked)
        },
    ) {
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun RadioSettingsItem(
    icon: ImageVector,
    text: String,
    description: String? = null,
    selected: Boolean = false,
    onLongClick: (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    RadioSettingsItem(
        icon = rememberVectorPainter(image = icon),
        text = text,
        description = description,
        selected = selected,
        onLongClick = onLongClick,
        onClick = onClick
    )
}

@Composable
fun RadioSettingsItem(
    icon: Painter,
    text: String,
    description: String? = null,
    selected: Boolean = false,
    onLongClick: (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    BaseSettingsItem(
        modifier = Modifier
            .combinedClickable(
                role = Role.RadioButton,
                interactionSource = remember { MutableInteractionSource() },
                indication = LocalIndication.current,
                onLongClick = onLongClick,
                onClick = {
                    onClick?.invoke()
                }
            )
            .semantics {
                this.selected = selected
            },
        icon = icon,
        text = text,
        descriptionText = description,
    ) {
        RadioButton(selected = selected, onClick = null)
    }
}

@Composable
fun ColorSettingsItem(
    icon: ImageVector,
    text: String,
    description: String? = null,
    onClick: (() -> Unit)? = null,
    initColor: Color,
) {
    ColorSettingsItem(
        icon = rememberVectorPainter(image = icon),
        text = text,
        description = description,
        onClick = onClick,
        initColor = initColor,
    )
}

@Composable
fun ColorSettingsItem(
    icon: Painter,
    text: String,
    description: String? = null,
    onClick: (() -> Unit)? = null,
    initColor: Color,
) {
    BaseSettingsItem(
        icon = icon,
        text = text,
        descriptionText = description,
        onClick = onClick
    ) {
        IconButton(onClick = { onClick?.invoke() }) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = initColor,
                        shape = RoundedCornerShape(50.dp)
                    )
            )
        }
    }
}

@Composable
fun BaseSettingsItem(
    modifier: Modifier = Modifier,
    icon: Painter,
    text: String,
    descriptionText: String? = null,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    content: (@Composable () -> Unit)? = null
) {
    BaseSettingsItem(
        modifier = modifier,
        icon = icon,
        text = text,
        description = if (descriptionText != null) {
            {
                Text(
                    modifier = Modifier.padding(top = 5.dp),
                    text = descriptionText,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        } else null,
        onClick = onClick,
        onLongClick = onLongClick,
        content = content,
    )
}

@Composable
fun BaseSettingsItem(
    modifier: Modifier = Modifier,
    icon: Painter,
    text: String,
    description: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    content: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .run {
                if (onClick != null) combinedClickable(onLongClick = onLongClick) { onClick() }
                else this
            }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier
                .padding(10.dp)
                .size(24.dp),
            painter = icon,
            contentDescription = null
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 10.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
            if (description != null) {
                Box(modifier = Modifier.padding(top = 5.dp)) {
                    description.invoke()
                }
            }
        }
        content?.let {
            Box(modifier = Modifier.padding(end = 5.dp)) { it.invoke() }
        }
    }
}

@Composable
fun CategorySettingsItem(text: String) {
    Text(
        modifier = Modifier.padding(
            start = 16.dp + 10.dp + 24.dp + 10.dp + 10.dp,
            end = 20.dp,
            top = 10.dp,
            bottom = 5.dp
        ),
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        maxLines = 3,
        overflow = TextOverflow.Ellipsis,
    )
}