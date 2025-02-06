package com.vz.p2pstreaming

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.view.Surface
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun StreamingScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var pipSupported by remember { mutableStateOf(false) }
    var showAlert by remember { mutableStateOf(false) }
    var hasCameraPermission by remember { mutableStateOf(false) }
    var isStreaming by remember { mutableStateOf(false) }
    var isDualCameraSupported by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        if (!granted) showAlert = true
    }

    LaunchedEffect(Unit) {
        hasCameraPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }

        isDualCameraSupported = isCamera2DualSupported(context)

        pipSupported = isPipSupported(context)
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when {
            hasCameraPermission -> {
                if (isStreaming) {
                    if (isDualCameraSupported) {
                        DualCameraPreview(modifier = Modifier.fillMaxSize())
                    } else {
                        CameraPreview(
                            modifier = Modifier.fillMaxSize(),
                            cameraSelector = TODO()
                        ) { surface ->
                            // Future: Pass surface to WebRTC for streaming
                        }
                    }
                } else {
                    StreamingControls { isStreaming = true }
                }
            }
            !hasCameraPermission -> Text("Camera permission required")
        }

        if (showAlert) {
            AlertDialog(
                onDismissRequest = { showAlert = false },
                title = { Text("Feature Unavailable") },
                text = { Text(
                    if (!hasCameraPermission) "Camera permission required for streaming"
                    else "PiP camera feature not supported on this device"
                ) },
                confirmButton = {
                    TextButton(onClick = { showAlert = false }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}

@Composable
private fun StreamingControls(onStartStreaming: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(onClick = onStartStreaming) {
            Text("Start Streaming")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { /* Future: Connect & Receive Stream */ }) {
            Text("Receive Stream")
        }
    }
}

/** Check if the device supports Camera2 logical multi-camera */
fun isCamera2DualSupported(context: Context): Boolean {
    return try {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        for (cameraId in cameraManager.cameraIdList) {
            val characteristics = cameraManager.getCameraCharacteristics(cameraId)
            val capabilities = characteristics.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES)
            if (capabilities?.contains(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_LOGICAL_MULTI_CAMERA) == true) {
                return true
            }
        }
        false
    } catch (e: Exception) {
        false
    }
}

/** Check if Picture-in-Picture mode is supported */
fun isPipSupported(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        context.packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
    } else {
        false
    }
}
