package com.vz.p2pstreaming

import android.content.Context
import android.graphics.SurfaceTexture
import android.util.Log
import android.view.Surface
import android.view.TextureView
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun CameraPreview(modifier: Modifier = Modifier, onSurfaceReady: (Surface) -> Unit, onStopStreaming: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isStreaming by remember { mutableStateOf(true) }

    Column(modifier = modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.weight(1f),
            factory = { ctx ->
                val textureView = TextureView(ctx)
                coroutineScope.launch {
                    startCamera(ctx, textureView, onSurfaceReady)
                }
                textureView
            }
        )
        Button(
            onClick = {
                isStreaming = false
                onStopStreaming()
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Stop Streaming")
        }
    }
}

private suspend fun startCamera(
    context: Context,
    textureView: TextureView,
    onSurfaceReady: (Surface) -> Unit
) {
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
    val cameraProvider = withContext(Dispatchers.IO){cameraProviderFuture.get()}
    val preview = Preview.Builder().build().also {
        it.setSurfaceProvider { request ->
            val surfaceTexture: SurfaceTexture = textureView.surfaceTexture ?: return@setSurfaceProvider
            val surface = Surface(surfaceTexture)
            onSurfaceReady(surface)
            request.provideSurface(surface,ContextCompat.getMainExecutor(context)){
                result ->
                Log.d("Camera Preview", "Surface provided: $result")
            }
        }
    }
    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    withContext(Dispatchers.Main) {
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(context as androidx.lifecycle.LifecycleOwner, cameraSelector, preview)
    }
}