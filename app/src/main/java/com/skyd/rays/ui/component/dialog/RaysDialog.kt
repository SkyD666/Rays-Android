/**
 * Copyright (C) 2023 Ashinch
 *
 * @link https://github.com/Ashinch/ReadYou
 * @author Ashinch
 * @modifier SkyD666
 */
package com.skyd.rays.ui.component.dialog

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.skyd.rays.R
import com.skyd.rays.ui.component.RaysLottieAnimation

@Composable
fun RaysDialog(
    modifier: Modifier = Modifier,
    visible: Boolean,
    properties: DialogProperties = DialogProperties(),
    onDismissRequest: () -> Unit = {},
    icon: @Composable (() -> Unit)? = {
        val res = remember {
            arrayOf(
                R.raw.lottie_genshin_impact_diona_1,
                R.raw.lottie_genshin_impact_klee_1,
                R.raw.lottie_genshin_impact_klee_2,
                R.raw.lottie_genshin_impact_klee_3,
                R.raw.lottie_genshin_impact_paimon_1,
                R.raw.lottie_genshin_impact_venti_1,
            )
        }
        RaysLottieAnimation(
            modifier = Modifier.size(48.dp),
            resId = res.random()
        )
    },
    title: @Composable (() -> Unit)? = null,
    text: @Composable (() -> Unit)? = null,
    selectable: Boolean = true,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable (() -> Unit)? = null,
) {
    if (visible) {
        AlertDialog(
            properties = properties,
            modifier = modifier,
            onDismissRequest = onDismissRequest,
            icon = icon,
            title = title,
            text = {
                if (selectable) {
                    SelectionContainer(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        text?.invoke()
                    }
                } else {
                    text?.invoke()
                }
            },
            confirmButton = confirmButton,
            dismissButton = dismissButton,
        )
    }
}