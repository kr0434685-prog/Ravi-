package com.example.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

class PermissionManager(private val activity: ComponentActivity) {
    
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private var onPermissionResult: ((Boolean) -> Unit)? = null

    init {
        requestPermissionLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            onPermissionResult?.invoke(isGranted)
        }
    }

    fun hasAudioPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun requestAudioPermission(onResult: (Boolean) -> Unit) {
        onPermissionResult = onResult
        requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }
}
