package com.skyd.rays.util

import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import com.skyd.rays.ui.local.LocalPickImageMethod

@Composable
fun rememberImagePicker(
    method: String = LocalPickImageMethod.current,
    multiple: Boolean = false,
    onResult: (List<Uri?>) -> Unit
): ManagedActivityResultLauncher<*, *> {
    return when (method) {
        "PickVisualMedia" -> if (multiple) rememberLauncherForActivityResult(
            ActivityResultContracts.PickMultipleVisualMedia(),
            onResult = onResult,
        ) else rememberLauncherForActivityResult(
            ActivityResultContracts.PickVisualMedia(),
            onResult = { onResult(listOf(it)) },
        )

        "OpenDocument" -> if (multiple) rememberLauncherForActivityResult(
            ActivityResultContracts.OpenMultipleDocuments(),
            onResult = onResult,
        ) else rememberLauncherForActivityResult(
            ActivityResultContracts.OpenDocument(),
            onResult = { onResult(listOf(it)) },
        )

        else -> if (multiple) rememberLauncherForActivityResult(
            ActivityResultContracts.GetMultipleContents(),
            onResult = onResult,
        ) else rememberLauncherForActivityResult(
            ActivityResultContracts.GetContent(),
            onResult = { onResult(listOf(it)) },
        )
    }
}

@Suppress("UNCHECKED_CAST")
fun ManagedActivityResultLauncher<*, *>.launchImagePicker() {
    when (contract) {
        is ActivityResultContracts.PickMultipleVisualMedia -> {
            (this as ManagedActivityResultLauncher<PickVisualMediaRequest, List<@JvmSuppressWildcards Uri>>)
                .launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        is ActivityResultContracts.PickVisualMedia -> {
            (this as ManagedActivityResultLauncher<PickVisualMediaRequest, Uri?>)
                .launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        is ActivityResultContracts.OpenMultipleDocuments -> {
            (this as ManagedActivityResultLauncher<Array<String>, List<@JvmSuppressWildcards Uri>>)
                .launch(arrayOf("image/*"))
        }

        is ActivityResultContracts.OpenDocument -> {
            (this as ManagedActivityResultLauncher<Array<String>, Uri?>)
                .launch(arrayOf("image/*"))
        }

        is ActivityResultContracts.GetMultipleContents -> {
            (this as ManagedActivityResultLauncher<String, List<@JvmSuppressWildcards Uri>>)
                .launch("image/*")
        }

        is ActivityResultContracts.GetContent -> {
            (this as ManagedActivityResultLauncher<String, Uri?>)
                .launch("image/*")
        }

        else -> error("Unknown contract type!")
    }
}