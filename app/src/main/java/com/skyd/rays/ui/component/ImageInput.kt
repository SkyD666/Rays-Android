package com.skyd.rays.ui.component

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.EventListener
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import com.skyd.compone.component.ComponeIconButton
import com.skyd.compone.component.ComponeIconButtonStyle
import com.skyd.rays.R

@Composable
fun ImageInput(
    modifier: Modifier = Modifier,
    title: String,
    hintText: String? = null,
    shape: Shape,
    imageUri: Uri?,
    maxImageHeight: Dp = Dp.Infinity,
    contentScale: ContentScale = ContentScale.FillWidth,
    onSelectImage: () -> Unit,
    onRemoveClick: (() -> Unit)? = null,
) {
    OutlinedCard(modifier = modifier, onClick = onSelectImage) {
        AnimatedVisibility(
            visible = imageUri == null,
            modifier = Modifier.clickable(onClick = onSelectImage)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .padding(6.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = shape,
                        )
                        .padding(16.dp)
                ) {
                    Icon(
                        modifier = Modifier.size(40.dp),
                        imageVector = Icons.Outlined.AddPhotoAlternate,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                    )
                }
                if (hintText != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        modifier = Modifier.padding(horizontal = 6.dp),
                        text = hintText,
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        AnimatedVisibility(
            visible = imageUri != null,
            modifier = Modifier.clickable(onClick = onSelectImage)
        ) {
            Box {
                Column {
                    var imageLoadError by rememberSaveable(imageUri) { mutableStateOf(false) }
                    if (imageLoadError) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = maxImageHeight)
                                .padding(horizontal = 12.dp)
                                .padding(top = 16.dp, bottom = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ErrorOutline,
                                contentDescription = null,
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = stringResource(R.string.image_load_error),
                                modifier = Modifier.basicMarquee(),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    } else {
                        RaysImage(
                            model = imageUri,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 50.dp, max = maxImageHeight),
                            blur = false,
                            contentDescription = null,
                            imageLoader = rememberRaysImageLoader(object : EventListener() {
                                override fun onError(request: ImageRequest, result: ErrorResult) {
                                    imageLoadError = true
                                }
                            }),
                            contentScale = contentScale,
                        )
                    }
                    Text(
                        modifier = Modifier
                            .padding(6.dp)
                            .align(Alignment.CenterHorizontally),
                        text = title,
                    )
                }
                if (onRemoveClick != null) {
                    ComponeIconButton(
                        modifier = Modifier.align(Alignment.TopEnd),
                        onClick = onRemoveClick,
                        imageVector = Icons.Outlined.Close,
                        style = ComponeIconButtonStyle.Filled,
                    )
                }
            }
        }
    }
}
