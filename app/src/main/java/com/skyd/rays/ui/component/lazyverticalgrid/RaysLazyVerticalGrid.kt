package com.skyd.rays.ui.component.lazyverticalgrid

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import com.skyd.rays.ext.simpleVerticalScrollbar
import com.skyd.rays.ui.component.lazyverticalgrid.adapter.AnimeItemSpace.animeItemSpace
import com.skyd.rays.ui.component.lazyverticalgrid.adapter.LazyGridAdapter
import com.skyd.rays.ui.component.lazyverticalgrid.adapter.MAX_SPAN_SIZE
import com.skyd.rays.ui.component.lazyverticalgrid.adapter.animeShowSpan

@Composable
fun RaysLazyVerticalGrid(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    dataList: List<Any>,
    adapter: LazyGridAdapter,
    enableLandScape: Boolean = true,     // 是否启用横屏使用另一套布局方案
    key: ((index: Int, item: Any) -> Any)? = null
) {
    val listState = rememberLazyGridState()
    val spanIndexArray: MutableList<Int> = remember { mutableListOf() }
    val configuration = LocalConfiguration.current
    LazyVerticalGrid(
        modifier = modifier.simpleVerticalScrollbar(listState),
        columns = GridCells.Fixed(MAX_SPAN_SIZE),
        state = listState,
        contentPadding = contentPadding
    ) {
        itemsIndexed(
            items = dataList,
            key = key,
            span = { index, item ->
                val spanIndex = maxLineSpan - maxCurrentLineSpan
                if (spanIndexArray.size > index) spanIndexArray[index] = spanIndex
                else spanIndexArray.add(spanIndex)
                GridItemSpan(
                    animeShowSpan(
                        data = item,
                        enableLandScape = enableLandScape,
                        configuration = configuration
                    )
                )
            }
        ) { index, item ->
            adapter.Draw(
                modifier = Modifier.animeItemSpace(
                    item = item,
                    spanSize = animeShowSpan(data = item, configuration = configuration),
                    spanIndex = spanIndexArray[index]
                ),
                index = index,
                data = item
            )
        }
    }
}