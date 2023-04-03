package com.skyd.rays.ui.component.lazyverticalgrid.adapter.proxy

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.skyd.rays.model.bean.StickerWithTags1
import com.skyd.rays.ui.component.RaysImage
import com.skyd.rays.ui.component.lazyverticalgrid.adapter.LazyGridAdapter

class StickerWithTags1Proxy(
    private val onClickListener: ((data: StickerWithTags1) -> Unit)? = null
) : LazyGridAdapter.Proxy<StickerWithTags1>() {
    @Composable
    override fun Draw(modifier: Modifier, index: Int, data: StickerWithTags1) {
        StickerWithTags1Item(modifier = modifier, data = data, onClickListener = onClickListener)
    }
}

@Composable
fun StickerWithTags1Item(
    modifier: Modifier = Modifier,
    data: StickerWithTags1,
    onClickListener: ((data: StickerWithTags1) -> Unit)? = null
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onLongClick = {

                    },
                    onClick = {
                        onClickListener?.invoke(data)
                    }
                ),
        ) {
            RaysImage(modifier = Modifier.fillMaxWidth(), uuid = data.sticker.uuid)
            if (data.sticker.title.isNotBlank()) {
                Text(
                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 10.dp),
                    text = data.sticker.title,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1
                )
            }
        }
    }
}
