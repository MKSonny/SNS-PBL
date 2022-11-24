package com.example.firebasestoreandauth.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat

fun providePermissions(): Array<String> {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.CAMERA
        )
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
    } else {
        arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
        )
    }
}

fun filterPermission(context: Context, perms: Array<String>): Array<String> {
    return perms.filter {
        (ContextCompat.checkSelfPermission(
            context,
            it
        ) != PackageManager.PERMISSION_GRANTED)
    }.toTypedArray()
}
