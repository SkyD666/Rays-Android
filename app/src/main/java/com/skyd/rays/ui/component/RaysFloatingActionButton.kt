package com.skyd.rays.ui.component

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.FloatingActionButtonElevation
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.PlainTooltipBox
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

enum class RaysFloatingActionButtonStyle {
    Normal, Extended, Large, Small
}

@Composable
fun RaysFloatingActionButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    elevation: FloatingActionButtonElevation = FloatingActionButtonDefaults.elevation(),
    style: RaysFloatingActionButtonStyle = RaysFloatingActionButtonStyle.Normal,
    contentDescription: String? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable RowScope.() -> Unit
) {
    val floatingActionButton: @Composable (modifier: Modifier) -> Unit = {
        when (style) {
            RaysFloatingActionButtonStyle.Normal -> FloatingActionButton(
                modifier = it,
                onClick = onClick,
                elevation = elevation,
                interactionSource = interactionSource,
                content = { Row { content() } },
            )

            RaysFloatingActionButtonStyle.Extended -> ExtendedFloatingActionButton(
                modifier = it,
                onClick = onClick,
                elevation = elevation,
                interactionSource = interactionSource,
                content = { Row { content() } },
            )

            RaysFloatingActionButtonStyle.Large -> LargeFloatingActionButton(
                modifier = it,
                onClick = onClick,
                elevation = elevation,
                interactionSource = interactionSource,
                content = { Row { content() } },
            )

            RaysFloatingActionButtonStyle.Small -> SmallFloatingActionButton(
                modifier = it,
                onClick = onClick,
                elevation = elevation,
                interactionSource = interactionSource,
                content = { Row { content() } },
            )
        }
    }

    if (contentDescription.isNullOrEmpty()) {
        floatingActionButton(modifier = modifier)
    } else {
        PlainTooltipBox(
            modifier = modifier,
            tooltip = { Text(contentDescription) }
        ) {
            floatingActionButton(modifier = Modifier.tooltipAnchor())
        }
    }
}
