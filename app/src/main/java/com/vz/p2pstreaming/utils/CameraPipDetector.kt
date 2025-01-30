package com.vz.p2pstreaming.utils

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.util.Log

class CameraPipDetector(private val context: Context) {
    private val TAG = "CameraPipDetector"
    fun isPipSupported(): Boolean {
        Log.d(TAG, "isPipSupported: ${isSystemPipEnabled()}")
        return isSystemPipEnabled()
    }

    private fun isSystemPipEnabled(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                context.packageManager.hasSystemFeature(
                    PackageManager.FEATURE_PICTURE_IN_PICTURE
                )
    }
}
