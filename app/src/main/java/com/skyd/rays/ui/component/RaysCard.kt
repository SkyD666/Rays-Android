package com.skyd.rays.ui.component

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role

@Composable
fun RaysCard(
    modifier: Modifier = Modifier,
    colors: CardColors = CardDefaults.cardColors(),
    onClickLabel: String? = null,
    role: Role? = null,
    onLongClickLabel: String? = null,
    onLongClick: (() -> Unit)? = null,
    onDoubleClick: (() -> Unit)? = null,
    onClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        colors = colors,
    ) {
        RaysCardColumn(
            onClickLabel = onClickLabel,
            role = role,
            onLongClickLabel = onLongClickLabel,
            onLongClick = onLongClick,
            onDoubleClick = onDoubleClick,
            onClick = onClick,
            content = content,
        )
    }
}

@Composable
fun RaysOutlinedCard(
    modifier: Modifier = Modifier,
    colors: CardColors = CardDefaults.cardColors(),
    onClickLabel: String? = null,
    role: Role? = null,
    onLongClickLabel: String? = null,
    onLongClick: (() -> Unit)? = null,
    onDoubleClick: (() -> Unit)? = null,
    onClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    OutlinedCard(
        modifier = modifier,
        colors = colors,
    ) {
        RaysCardColumn(
            onClickLabel = onClickLabel,
            role = role,
            onLongClickLabel = onLongClickLabel,
            onLongClick = onLongClick,
            onDoubleClick = onDoubleClick,
            onClick = onClick,
            content = content,
        )
    }
}

@Composable
private fun RaysCardColumn(
    onClickLabel: String? = null,
    role: Role? = null,
    onLongClickLabel: String? = null,
    onLongClick: (() -> Unit)? = null,
    onDoubleClick: (() -> Unit)? = null,
    onClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClickLabel = onClickLabel,
                role = role,
                onLongClickLabel = onLongClickLabel,
                onLongClick = onLongClick,
                onDoubleClick = onDoubleClick,
                onClick = onClick,
            )
    ) {
        content()
    }
}