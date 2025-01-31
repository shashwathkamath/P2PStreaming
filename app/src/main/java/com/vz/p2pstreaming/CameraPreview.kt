package com.vz.p2pstreaming

import android.content.Context
import android.graphics.SurfaceTexture
import android.util.Log
import android.view.Surface
import android.view.TextureView
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun CameraPreview(modifier: Modifier = Modifier, onSurfaceReady: (Surface) -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = {
            ctx ->
            val textureView = TextureView(ctx)
            coroutineScope.launch {
                startCamera(ctx,textureView,onSurfaceReady)
            }
            textureView
        }
    )
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