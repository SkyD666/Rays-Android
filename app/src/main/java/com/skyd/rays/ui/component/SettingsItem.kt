package com.skyd.rays.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun SwitchSettingsItem(
    icon: ImageVector,
    text: String,
    description: String? = null,
    checked: MutableState<Boolean> = mutableStateOf(false),
    onCheckedChange: ((Boolean) -> Unit)?,
) {
    SwitchSettingsItem(
        icon = rememberVectorPainter(image = icon),
        text = text,
        description = description,
        checked = checked,
        onCheckedChange = onCheckedChange
    )
}

@Composable
fun SwitchSettingsItem(
    icon: Painter,
    text: String,
    description: String? = null,
    checked: MutableState<Boolean> = mutableStateOf(false),
    onCheckedChange: ((Boolean) -> Unit)?,
) {
    BaseSettingsItem(
        icon = icon,
        text = text,
        descriptionText = description,
        onClick = {
            checked.value = !checked.value
            onCheckedChange?.invoke(checked.value)
        }
    ) {
        Switch(checked = checked.value, onCheckedChange = {
            checked.value = it
            onCheckedChange?.invoke(it)
        })
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
        icon = icon,
        text = text,
        descriptionText = description,
        onLongClick = onLongClick,
        onClick = onClick
    ) {
        RadioButton(selected = selected, onClick = onClick)
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
    icon: Painter,
    text: String,
    descriptionText: String? = null,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    content: (@Composable () -> Unit)? = null
) {
    BaseSettingsItem(
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
    icon: Painter,
    text: String,
    description: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    content: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier
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