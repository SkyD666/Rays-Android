package com.skyd.rays.ui.component

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter

enum class RaysIconButtonStyle {
    Normal, Filled, FilledTonal, Outlined
}

@Composable
fun RaysIconButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    painter: Painter,
    tint: Color? = null,
    style: RaysIconButtonStyle = RaysIconButtonStyle.Normal,
    contentDescription: String? = null,
    enabled: Boolean = true,
    colors: IconButtonColors? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    val iconButton: @Composable (modifier: Modifier) -> Unit = {
        val icon: @Composable () -> Unit = {
            Icon(
                painter = painter,
                tint = tint ?: LocalContentColor.current,
                contentDescription = contentDescription,
            )
        }
        when (style) {
            RaysIconButtonStyle.Normal -> IconButton(
                modifier = it,
                onClick = onClick,
                enabled = enabled,
                colors = colors ?: IconButtonDefaults.iconButtonColors(),
                interactionSource = interactionSource,
                content = icon,
            )
            RaysIconButtonStyle.Filled -> FilledIconButton(
                modifier = it,
                onClick = onClick,
                enabled = enabled,
                colors = colors ?: IconButtonDefaults.filledIconButtonColors(),
                interactionSource = interactionSource,
                content = icon,
            )
            RaysIconButtonStyle.FilledTonal -> FilledTonalIconButton(
                modifier = it,
                onClick = onClick,
                enabled = enabled,
                colors = colors ?: IconButtonDefaults.filledTonalIconButtonColors(),
                interactionSource = interactionSource,
                content = icon,
            )
            RaysIconButtonStyle.Outlined -> OutlinedIconButton(
                modifier = it,
                onClick = onClick,
                enabled = enabled,
                colors = colors ?: IconButtonDefaults.outlinedIconButtonColors(),
                interactionSource = interactionSource,
                content = icon,
            )
        }
    }

    if (contentDescription.isNullOrEmpty()) {
        iconButton(modifier = modifier)
    } else {
        TooltipBox(
            modifier = modifier,
            positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
            tooltip = {
                PlainTooltip {
                    Text(contentDescription)
                }
            },
            state = rememberTooltipState()
        ) {
            iconButton(modifier = Modifier)
        }
    }
}

@Composable
fun RaysIconButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    imageVector: ImageVector,
    tint: Color? = null,
    style: RaysIconButtonStyle = RaysIconButtonStyle.Normal,
    contentDescription: String? = null,
    enabled: Boolean = true,
    colors: IconButtonColors? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    RaysIconButton(
        modifier = modifier,
        onClick = onClick,
        painter = rememberVectorPainter(image = imageVector),
        style = style,
        contentDescription = contentDescription,
        tint = tint,
        enabled = enabled,
        colors = colors,
        interactionSource = interactionSource,
    )
}
