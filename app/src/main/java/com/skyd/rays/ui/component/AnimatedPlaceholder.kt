package com.skyd.rays.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun AnimatedPlaceholder(
    @androidx.annotation.RawRes resId: Int,
    tip: String,
    sizeFraction: Float = 0.5f,
    onClick: (() -> Unit)? = null
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.fillMaxSize(fraction = sizeFraction).run {
                if (onClick != null) {
                    clickable(onClick = onClick)
                } else this
            },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            RaysLottieAnimation(
                modifier = Modifier.weight(1f),
                resId = resId,
            )
            Text(
                modifier = Modifier.padding(top = 10.dp),
                text = tip,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}