package com.vz.p2pstreaming

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.vz.p2pstreaming.utils.CameraPipDetector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun StreamingScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var pipSupported by remember { mutableStateOf(false) }
    var showAlert by remember { mutableStateOf(false) }
    var hasCameraPermission by remember { mutableStateOf(false) }
    var isStreaming by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        if (!granted) showAlert = true
    }

    LaunchedEffect(Unit) {
        hasCameraPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED

        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
        pipSupported = withContext(Dispatchers.IO) {
            CameraPipDetector(context).isPipSupported()
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when {
            pipSupported && hasCameraPermission -> if (isStreaming) {
                CameraPreview(
                    modifier = Modifier.fillMaxSize(),
                    onSurfaceReady = { surface -> /* Start streaming */ },
                    onStopStreaming = { isStreaming = false }
                )
            } else {
                StreamingControls { isStreaming = true }
            }
            !pipSupported -> PipUnsupportedWarning { showAlert = true }
            !hasCameraPermission -> Text("Camera permission required")
        }

        if (showAlert) {
            AlertDialog(
                onDismissRequest = { showAlert = false },
                title = { Text("Feature Unavailable") },
                text = {
                    Text(
                        if (!hasCameraPermission) "Camera permission required for streaming"
                        else "PiP camera feature not supported on this device"
                    )
                },
                confirmButton = {
                    TextButton({ showAlert = false }) {
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

@Composable
private fun PipUnsupportedWarning(onShowAlert: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("PiP Camera Not Supported")
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onShowAlert) {
            Text("Show Details")
        }
    }
}
