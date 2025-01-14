package com.skyd.rays.ui.component

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun ImageInput(
    modifier: Modifier = Modifier,
    title: String,
    hintText: String? = null,
    shape: Shape,
    imageUri: Uri?,
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
                    RaysImage(
                        model = imageUri,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 50.dp),
                        blur = false,
                        contentDescription = null,
                        contentScale = contentScale,
                    )
                    Text(
                        modifier = Modifier
                            .padding(6.dp)
                            .align(Alignment.CenterHorizontally),
                        text = title,
                    )
                }
                if (onRemoveClick != null) {
                    RaysIconButton(
                        modifier = Modifier.align(Alignment.TopEnd),
                        onClick = onRemoveClick,
                        imageVector = Icons.Outlined.Close,
                        style = RaysIconButtonStyle.Filled,
                    )
                }
            }
        }
    }
}
