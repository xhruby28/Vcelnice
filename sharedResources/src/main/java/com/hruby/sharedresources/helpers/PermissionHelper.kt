package com.hruby.sharedresources.helpers

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat

object PermissionHelper {

    // Zkontrolujte, zda jsou všechna oprávnění udělena
    fun hasPermissions(context: Context, permissions: Array<String>): Boolean {
        return permissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    // Požádejte o oprávnění
    fun requestPermissions(
        launcher: ActivityResultLauncher<Array<String>>,
        permissions: Array<String>
    ) {
        launcher.launch(permissions)
    }

    // Specifické sady oprávnění
    val bluetoothPermissions: Array<String>
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                android.Manifest.permission.BLUETOOTH_SCAN,
                android.Manifest.permission.BLUETOOTH_CONNECT
            )
        } else emptyArray()

    val wifiPermissions: Array<String>
        get() =
            arrayOf(
                android.Manifest.permission.ACCESS_WIFI_STATE,
                android.Manifest.permission.CHANGE_WIFI_STATE
            )

    val storagePermissionsRead: Array<String>
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(android.Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }

    val storagePermissionsWrite: Array<String>
        get() = arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

    val cameraPermissions: Array<String>
        get() = arrayOf(android.Manifest.permission.CAMERA)
}