package com.skyd.rays.ui.component.lazyverticalgrid.adapter.proxy

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.skyd.rays.model.bean.More1Bean
import com.skyd.rays.ui.component.lazyverticalgrid.adapter.LazyGridAdapter

class More1Proxy(
    private val onClickListener: ((data: More1Bean) -> Unit)? = null
) : LazyGridAdapter.Proxy<More1Bean>() {
    @Composable
    override fun Draw(modifier: Modifier, index: Int, data: More1Bean) {
        More1Item(modifier = modifier, data = data, onClickListener = onClickListener)
    }
}

@Composable
fun More1Item(
    modifier: Modifier,
    data: More1Bean,
    onClickListener: ((data: More1Bean) -> Unit)? = null
) {
    OutlinedCard(
        modifier = modifier.padding(vertical = 6.dp),
        shape = RoundedCornerShape(16)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = {
                        onClickListener?.invoke(data)
                    }
                )
                .padding(25.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .padding(5.dp)
                    .background(
                        color = data.shapeColor,
                        shape = data.shape
                    )
                    .padding(16.dp)
            ) {
                Icon(
                    modifier = Modifier.size(35.dp),
                    imageVector = data.icon,
                    contentDescription = null,
                    tint = data.iconTint
                )
            }
            Text(
                modifier = Modifier
                    .padding(horizontal = 5.dp)
                    .padding(top = 15.dp),
                text = data.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                textAlign = TextAlign.Center
            )
        }
    }
}
