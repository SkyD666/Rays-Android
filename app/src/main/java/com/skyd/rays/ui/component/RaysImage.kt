package com.skyd.rays.ui.component

import android.os.Build
import android.os.Build.VERSION.SDK_INT
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.skyd.rays.config.STICKER_DIR
import com.skyd.rays.ext.dataStore
import com.skyd.rays.ext.getOrDefault
import com.skyd.rays.model.preference.privacy.BlurStickerRadiusPreference
import com.skyd.rays.ui.local.LocalBlurStickerRadius
import com.skyd.rays.util.coil.BlurTransformation
import com.skyd.rays.util.coil.apng.AnimatedPngDecoder
import java.io.File


@Composable
fun RaysImage(
    model: Any?,
    blur: Boolean,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    imageLoader: ImageLoader = rememberRaysImageLoader(),
    contentScale: ContentScale = ContentScale.FillWidth,
    alpha: Float = DefaultAlpha,
) {
    val context = LocalContext.current
    AsyncImage(
        model = remember(model, blur) {
            ImageRequest.Builder(context)
                .data(model)
                .crossfade(true)
                .decoderFactory(AnimatedPngDecoder.Factory())
                .run {
                    if (SDK_INT < Build.VERSION_CODES.S && blur) transformations(
                        BlurTransformation(
                            context = context,
                            radius = context.dataStore.getOrDefault(BlurStickerRadiusPreference),
                            sampling = 3f
                        )
                    )
                    else this
                }
                .build()
        },
        modifier = modifier.run {
            if (blur) blur(radius = LocalBlurStickerRadius.current.dp) else this
        },
        contentDescription = contentDescription,
        contentScale = contentScale,
        imageLoader = imageLoader,
        alpha = alpha,
    )
}

@Composable
fun RaysImage(
    uuid: String,
    blur: Boolean,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    imageLoader: ImageLoader = rememberRaysImageLoader(),
    contentScale: ContentScale = ContentScale.FillWidth,
) {
    val context = LocalContext.current
    val file = remember(uuid) { File(context.STICKER_DIR, uuid) }
    RaysImage(
        model = file,
        blur = blur,
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