package com.skyd.rays.ui.component

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
import coil.request.ImageRequest
import com.skyd.rays.config.STICKER_DIR
import java.io.File


@Composable
fun RaysImage(
    model: Any?,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    imageLoader: ImageLoader = rememberRaysImageLoader(),
    contentScale: ContentScale = ContentScale.FillWidth,
) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(model)
            .crossfade(true)
            .build(),
        modifier = modifier,
        contentDescription = contentDescription,
        contentScale = contentScale,
        imageLoader = imageLoader,
    )
}

@Composable
fun RaysImage(
    uuid: String,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    imageLoader: ImageLoader = rememberRaysImageLoader(),
    contentScale: ContentScale = ContentScale.FillWidth,
) {
    val file = remember(uuid) { File(STICKER_DIR, uuid) }
    RaysImage(
        model = file,
        modifier = modifier,
        contentDescription = contentDescription,
        contentScale = contentScale,
        imageLoader = imageLoader,
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