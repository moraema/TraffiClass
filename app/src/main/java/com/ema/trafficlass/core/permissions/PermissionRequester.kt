package com.ema.trafficlass.core.permissions

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable

@Composable
fun rememberPermissionRequester(
    onResult: (granted: Boolean) -> Unit
): () -> Unit {
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        onResult(granted)
    }

    return {
        launcher.launch(Manifest.permission.CAMERA)
    }
}
