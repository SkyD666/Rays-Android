package com.skyd.rays.ui.screen.fullimage

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import com.skyd.rays.ext.navigate
import com.skyd.rays.ui.component.RaysTopBar
import me.saket.telephoto.subsamplingimage.SubSamplingImage
import me.saket.telephoto.subsamplingimage.SubSamplingImageSource
import me.saket.telephoto.subsamplingimage.rememberSubSamplingImageState
import me.saket.telephoto.zoomable.ZoomSpec
import me.saket.telephoto.zoomable.rememberZoomableState
import me.saket.telephoto.zoomable.zoomable

const val FULL_IMAGE_SCREEN_ROUTE = "fullImageScreen"
const val FULL_IMAGE_SCREEN_IMAGE_KEY = "image"

fun openFullImageScreen(
    navController: NavHostController,
    image: Uri
) {
    navController.navigate(
        FULL_IMAGE_SCREEN_ROUTE,
        Bundle().apply {
            putParcelable(FULL_IMAGE_SCREEN_IMAGE_KEY, image)
        }
    )
}

@Composable
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
fun FullImageScreen(image: Uri) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }
    var showWidget by rememberSaveable { mutableStateOf(true) }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            AnimatedVisibility(
                visible = showWidget,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                RaysTopBar(
                    scrollBehavior = scrollBehavior,
                    title = { },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Black.copy(alpha = 0.4f),
                    ),
                )
            }
        },
        containerColor = Color.Black,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            val zoomableState = rememberZoomableState(zoomSpec = ZoomSpec(maxZoomFactor = 4f))
            val imageState = rememberSubSamplingImageState(
                zoomableState = zoomableState,
                imageSource = SubSamplingImageSource.contentUri(image),
            )
            SubSamplingImage(
                modifier = Modifier
                    .fillMaxSize()
                    .zoomable(zoomableState, onClick = { showWidget = !showWidget }),
                state = imageState,
                contentDescription = null,
            )
        }
    }
}