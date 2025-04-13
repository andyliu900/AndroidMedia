/*
 * Copyright (C) 2025 Baidu, Inc. All Rights Reserved.
 */
package com.ideacode.android_audio.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.AudioRouting.OnRoutingChangedListener
import android.media.ExifInterface
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.OrientationEventListener
import android.view.Surface
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCapture.OnImageSavedCallback
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.google.android.material.appbar.MaterialToolbar
import com.ideacode.android_audio_record.R
import java.io.File

/**
 * Copyright (C), 2021-2025, 无业游民
 * @ProjectName:    AndroidMedia01
 * @Package:        com.ideacode.android_audio.ui
 * @ClassName:      CameraXActivity
 * @Description:
 * @Author:         randysu
 * @CreateDate:     2025/4/12 21:22
 * @UpdateUser:
 * @UpdateDate:     2025/4/12 21:22
 * @UpdateRemark:
 * @Version:        1.0
 */
class CameraXActivity : AppCompatActivity() {

    companion object {
        const val TAG = "CameraXActivity"
    }

    lateinit var toolbar: MaterialToolbar
    lateinit var switch_camera: Button
    lateinit var camera_capture: Button
    lateinit var camera_preview: PreviewView

    lateinit var PATH: String

    var mFacing:Int = CameraSelector.LENS_FACING_BACK
    lateinit var imageCapture: ImageCapture

    var currentRotation = Surface.ROTATION_0 // 默认方向
    val orientationEventListener by lazy {
        object: OrientationEventListener(this) {
            override fun onOrientationChanged(orientation: Int) {
                Log.d(TAG, "onOrientationChanged  orientation: ${orientation}")
                currentRotation = when {
                    orientation in 45..135 -> Surface.ROTATION_270
                    orientation in 135..225 -> Surface.ROTATION_180
                    orientation in 225..315 -> Surface.ROTATION_90
                    else -> Surface.ROTATION_0
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camerax)

        toolbar = findViewById(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            this@CameraXActivity.finish()
        }

        switch_camera = findViewById(R.id.switch_camera)
        switch_camera.setOnClickListener {
            switchCamera()
        }

        camera_capture = findViewById(R.id.camera_capture)
        camera_capture.setOnClickListener {
            cameraCapture()
        }

        camera_preview = findViewById(R.id.camera_preview)

        PATH = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.absolutePath + "/VideoDemo"

        startCamera()
    }

    override fun onStart() {
        super.onStart()
        orientationEventListener.enable()
    }

    override fun onStop() {
        super.onStop()
        orientationEventListener.disable()
    }

    // 开启摄像头
    fun startCamera() {
        var cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            try {
                // 将相机的生命周期和 activity 生命周期绑定
                val cameraProvider = cameraProviderFuture.get()
                // 预览 capture，支持角度转换
                val preview = Preview.Builder()
                    .setTargetRotation(currentRotation)
                    .build().also {
                        it.setSurfaceProvider(camera_preview.surfaceProvider)
                    }

                // 创建图片的 capture
                imageCapture = ImageCapture.Builder()
                    .setTargetRotation(camera_preview.display.rotation)
                    .setFlashMode(ImageCapture.FLASH_MODE_AUTO)
                    .build()

                // 选择后置摄像头
                val cameraSlactor = CameraSelector.Builder().requireLensFacing(mFacing).build()

                // 预览之前先接棒
                cameraProvider.unbindAll()

                // 将数据绑定到相机的生命周期中
                val camera = cameraProvider.bindToLifecycle(this@CameraXActivity, cameraSlactor,
                    preview, imageCapture)

//                // 将 preview 的 surface 给相机进行内容预览
//                preview.setSurfaceProvider(camera_preview.surfaceProvider)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    fun switchCamera() {
        mFacing = if (mFacing == CameraSelector.LENS_FACING_BACK) CameraSelector
            .LENS_FACING_FRONT else  CameraSelector.LENS_FACING_BACK
        startCamera()
    }

    fun cameraCapture() {
        val dir = File(PATH)
        if (!dir.exists()) {
            dir.mkdirs()
        }

        // 创建文件
        val photoFile = File(PATH, "testx.jpg")
        if (photoFile.exists()) {
            photoFile.delete()
        }

        // 创建包文件的数据
        var outputFileOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // 开始拍照
        imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor
            (this@CameraXActivity), object: OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                runOnUiThread {
                    val bitmap = fixImageRotation(photoFile)
                    Toast.makeText(this@CameraXActivity, "保存成功", Toast.LENGTH_LONG).show()
                }

//                correctImageOrientation(photoFile)
            }

            override fun onError(exception: ImageCaptureException) {
                Toast.makeText(this@CameraXActivity, "保存失败", Toast.LENGTH_LONG).show()
            }
        })
    }

    fun correctImageOrientation(outputFile: File) {
        Log.e(TAG, "correctImageOrientation  currentRotation: ${currentRotation}")

        val exif = ExifInterface(outputFile.absolutePath)
        val orientation = when (currentRotation) {
            Surface.ROTATION_90 -> ExifInterface.ORIENTATION_ROTATE_90
            Surface.ROTATION_180 -> ExifInterface.ORIENTATION_ROTATE_180
            Surface.ROTATION_270 -> ExifInterface.ORIENTATION_ROTATE_270
            else -> ExifInterface.ORIENTATION_NORMAL
        }
        exif.setAttribute(ExifInterface.TAG_ORIENTATION, orientation.toString())
        exif.saveAttributes()
    }

    fun fixImageRotation(file: File): Bitmap {
        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
        val exif = ExifInterface(file.absolutePath)
        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

        val matrix = Matrix()
        val routationDegress = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }

        matrix.postRotate(routationDegress)
        return Bitmap.createBitmap(bitmap, 0, 0 ,bitmap.width, bitmap.height, matrix, true)
    }

}