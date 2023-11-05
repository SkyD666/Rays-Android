package com.skyd.rays.ui.component.dialog

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.skyd.rays.R
import com.skyd.rays.ui.component.RaysLottieAnimation

@Composable
fun DeleteWarningDialog(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    RaysDialog(
        visible = visible,
        onDismissRequest = onDismissRequest,
        icon = {
            RaysLottieAnimation(
                modifier = Modifier.size(48.dp),
                resId = R.raw.lottie_genshin_impact_qiqi_1
            )
        },
        title = {
            Text(text = stringResource(R.string.dialog_warning))
        },
        text = {
            Text(text = stringResource(R.string.home_screen_delete_warning))
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.dialog_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}