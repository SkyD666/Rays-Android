package com.skyd.rays.ui.component.dialog

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.skyd.rays.R
import com.skyd.rays.ui.component.RaysLottieAnimation

@Composable
fun WaitingDialog(
    visible: Boolean,
    currentValue: Int? = null,
    totalValue: Int? = null,
    msg: String? = null,
    title: String = stringResource(R.string.webdav_screen_waiting)
) {
    if (currentValue == null || totalValue == null) {
        WaitingDialog(visible = visible, title = title)
    } else {
        val animatedProgress by animateFloatAsState(
            targetValue = currentValue.toFloat() / totalValue,
            animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
            label = "waitingDialogAnimatedProgress"
        )
        WaitingDialog(visible = visible, title = title) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LinearProgressIndicator(
                    modifier = Modifier.semantics(mergeDescendants = true) {},
                    progress = animatedProgress,
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "$currentValue / $totalValue",
                    style = MaterialTheme.typography.labelLarge
                )
                if (msg != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = msg,
                        style = MaterialTheme.typography.labelLarge,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
fun WaitingDialog(
    visible: Boolean,
    title: String = stringResource(R.string.webdav_screen_waiting),
    text: @Composable (() -> Unit)? = null,
) {
    RaysDialog(
        visible = visible,
        onDismissRequest = { },
        icon = {
            RaysLottieAnimation(
                modifier = Modifier.size(48.dp),
                resId = R.raw.lottie_genshin_impact_klee_3
            )
        },
        title = {
            Text(text = title)
        },
        text = text,
        confirmButton = {}
    )
}