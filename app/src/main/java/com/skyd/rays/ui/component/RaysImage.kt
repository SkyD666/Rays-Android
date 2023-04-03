package com.skyd.rays.ui.component

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.skyd.rays.config.STICKER_DIR
import java.io.File


@Composable
fun RaysImage(
    uri: Uri?,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    contentScale: ContentScale = ContentScale.FillWidth,
) {
    AsyncImage(
        modifier = modifier,
        contentDescription = contentDescription,
        contentScale = contentScale,
        model = uri,
    )
}

@Composable
fun RaysImage(
    uuid: String,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    contentScale: ContentScale = ContentScale.FillWidth,
) {
    val file = remember(uuid) { File(STICKER_DIR, uuid) }
    AsyncImage(
        modifier = modifier,
        contentDescription = contentDescription,
        contentScale = contentScale,
        model = file,
    )
}