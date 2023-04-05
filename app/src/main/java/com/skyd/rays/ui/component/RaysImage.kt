package com.skyd.rays.ui.component

import android.net.Uri
import android.os.Build.VERSION.SDK_INT
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
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
        model = uri,
        modifier = modifier,
        contentDescription = contentDescription,
        contentScale = contentScale,
        imageLoader = rememberRaysImageLoader(),
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
        model = file,
        modifier = modifier,
        contentDescription = contentDescription,
        contentScale = contentScale,
        imageLoader = rememberRaysImageLoader(),
    )
}

@Composable
private fun rememberRaysImageLoader(): ImageLoader {
    val context = LocalContext.current
    return remember(context) {
        ImageLoader.Builder(context)
            .components {
                if (SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .build()
    }
}