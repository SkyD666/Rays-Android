package com.skyd.rays.ui.screen.minitool

import android.content.Context
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material.icons.outlined.PeopleAlt
import androidx.compose.material.icons.outlined.Style
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.skyd.rays.R
import com.skyd.rays.ext.plus
import com.skyd.rays.ext.showSnackbar
import com.skyd.rays.model.bean.MiniTool1Bean
import com.skyd.rays.ui.component.RaysIconButton
import com.skyd.rays.ui.component.RaysTopBar
import com.skyd.rays.ui.component.RaysTopBarStyle
import com.skyd.rays.ui.local.LocalNavController
import com.skyd.rays.ui.screen.minitool.selfiesegmentation.SelfieSegmentationRoute
import com.skyd.rays.ui.screen.minitool.styletransfer.StyleTransferRoute
import kotlinx.serialization.Serializable


@Serializable
data object MiniToolRoute

@Composable
fun MiniToolScreen() {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val context = LocalContext.current
    val navController = LocalNavController.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            RaysTopBar(
                style = RaysTopBarStyle.Large,
                scrollBehavior = scrollBehavior,
                title = { Text(text = stringResource(id = R.string.mini_tool_screen_name)) },
                navigationIcon = {
                    RaysIconButton(
                        imageVector = Icons.Outlined.Extension,
                        onClick = {
                            snackbarHostState.showSnackbar(
                                message = "\ud83c\udfee Happy New Year 2025~",
                                scope = scope,
                                withDismissAction = true
                            )
                        }
                    )
                },
            )
        },
    ) {
        val miniToolList = remember {
            getMiniToolLost(context = context, navController = navController)
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = it + PaddingValues(vertical = 10.dp, horizontal = 16.dp),
        ) {
            items(miniToolList) { item ->
                MiniTool1Item(
                    data = item,
                    onClickListener = { data -> data.action.invoke() },
                )
            }
        }
    }
}

@Composable
private fun MiniTool1Item(
    data: MiniTool1Bean,
    onClickListener: ((data: MiniTool1Bean) -> Unit)? = null
) {
    val context = LocalContext.current
    OutlinedCard(
        modifier = Modifier.padding(vertical = 6.dp),
        shape = RoundedCornerShape(16)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = {
                        onClickListener?.invoke(data)
                    }
                )
                .padding(25.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier.size(35.dp),
                imageVector = data.icon,
                contentDescription = null,
            )
            BadgedBox(
                modifier = Modifier.padding(horizontal = 12.dp),
                badge = {
                    if (data.experimental) {
                        Text(
                            text = stringResource(R.string.mini_tool_experimental),
                            modifier = Modifier.semantics {
                                contentDescription =
                                    context.getString(R.string.mini_tool_experimental)
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            ) {
                Text(
                    text = data.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

private fun getMiniToolLost(
    context: Context,
    navController: NavController,
) = listOf(
    MiniTool1Bean(
        title = context.getString(R.string.style_transfer_screen_name),
        icon = Icons.Outlined.Style,
        action = { navController.navigate(StyleTransferRoute) }
    ),
    MiniTool1Bean(
        title = context.getString(R.string.selfie_segmentation_screen_name),
        icon = Icons.Outlined.PeopleAlt,
        action = { navController.navigate(SelfieSegmentationRoute) }
    ),
)