/*
 * Copyright (C) 2025 Baidu, Inc. All Rights Reserved.
 */
package com.ideacode.android_audio

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraX
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.ideacode.android_audio_record.R
import com.ideacode.android_audio.ui.AudioRecordActivity
import com.ideacode.android_audio.ui.CameraXActivity

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG: String = "Ideacode_MediaStudy"
    }

    private val requestPermissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {permissions ->
        permissions.entries.forEach {entry ->
            val permission = entry.key
            val isGranted = entry.value

            Log.d(TAG, "permission:${permission}  isGranted:${isGranted}")

            if (isGranted) {
                Log.d(TAG, "${permission} is granted.")
            } else {
                val shouldShow = ActivityCompat.shouldShowRequestPermissionRationale(this,
                    permission)

                Log.d(TAG, "permission:${permission}  shouldShow:${shouldShow}")

                if (!shouldShow) {
                    Log.d(TAG, "${permission} denied permanently")
                } else {
                    Log.d(TAG, "${permission} denied")
                }
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.request_permission).setOnClickListener() {
            checkAndRequestPermissions()
        }

        findViewById<Button>(R.id.audio_record).setOnClickListener() {
            startTargetActivity("AudioRecoed")
        }

        findViewById<Button>(R.id.camerax).setOnClickListener {
            startTargetActivity("CameraX")
        }
    }

    private fun startTargetActivity(targetName: String) {
        when (targetName) {
            "AudioRecoed" ->
                Intent(this, AudioRecordActivity::class.java).also {
                    startActivity(it)
                }
            "CameraX" ->
                Intent(this, CameraXActivity::class.java).also {
                    startActivity(it)
                }
        }
    }

    private fun checkAndRequestPermissions() {
        val permissionsNeeded = mutableListOf<String>()

        getRequestPermissions().forEach { permission ->
            Log.d(TAG, "request permission: ${permission}")

            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(permission)
            }
        }

        permissionsNeeded.forEach {permissionNeeded ->
            Log.d(TAG, "permissionNeeded: ${permissionNeeded}")
        }

        if (permissionsNeeded.isNotEmpty()) {
            requestPermissions.launch(permissionsNeeded.toTypedArray())
        } else {
            Toast.makeText(this, "所有权限都已经赋予", Toast.LENGTH_LONG).show()
        }
    }

    private fun getRequestPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA)
        } else {
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA)
        }
    }
}