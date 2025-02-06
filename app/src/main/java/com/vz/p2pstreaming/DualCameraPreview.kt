package com.vz.p2pstreaming

import android.content.Context
import android.view.Surface
import androidx.camera.core.CameraSelector
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import java.util.concurrent.Executors

@Composable
fun DualCameraPreview(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val executor = remember { Executors.newSingleThreadExecutor() }

    Column(modifier = modifier.fillMaxSize()) {
        CameraPreview(
            modifier = Modifier.weight(1f),
            onSurfaceReady = { surface: Surface ->
                // TODO: Handle front camera stream
            },
            cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
        )
        CameraPreview(
            modifier = Modifier.weight(1f),
            onSurfaceReady = { surface: Surface ->
                // TODO: Handle back camera stream
            },
            cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        )
    }
}
