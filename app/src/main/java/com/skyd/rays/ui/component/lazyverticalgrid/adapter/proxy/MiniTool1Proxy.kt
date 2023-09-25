package com.skyd.rays.ui.component.lazyverticalgrid.adapter.proxy

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.skyd.rays.R
import com.skyd.rays.model.bean.MiniTool1Bean
import com.skyd.rays.ui.component.lazyverticalgrid.adapter.LazyGridAdapter

class MiniTool1Proxy(
    private val onClickListener: ((data: MiniTool1Bean) -> Unit)? = null
) : LazyGridAdapter.Proxy<MiniTool1Bean>() {
    @Composable
    override fun Draw(modifier: Modifier, index: Int, data: MiniTool1Bean) {
        MiniTool1Item(modifier = modifier, data = data, onClickListener = onClickListener)
    }
}

@Composable
fun MiniTool1Item(
    modifier: Modifier,
    data: MiniTool1Bean,
    onClickListener: ((data: MiniTool1Bean) -> Unit)? = null
) {
    OutlinedCard(
        modifier = modifier.padding(vertical = 6.dp),
        shape = RoundedCornerShape(16)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = {
                        onClickListener?.invoke(data)
                    }
                )
                .padding(25.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier.size(35.dp),
                imageVector = data.icon,
                contentDescription = null,
            )
            Text(
                modifier = Modifier.padding(horizontal = 10.dp),
                text = data.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                textAlign = TextAlign.Center
            )
            if (data.experimental) {
                TooltipBox(
                    positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                    tooltip = { PlainTooltip { Text(stringResource(R.string.mini_tool_experimental)) } },
                    state = rememberTooltipState()
                ) {
                    Icon(
                        imageVector = Icons.Default.Science,
                        contentDescription = stringResource(R.string.mini_tool_experimental),
                        tint = LocalContentColor.current.copy(alpha = 0.5f),
                    )
                }
            }
        }
    }
}
