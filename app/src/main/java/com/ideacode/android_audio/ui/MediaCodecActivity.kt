/*
 * Copyright (C) 2025 Baidu, Inc. All Rights Reserved.
 */
package com.ideacode.android_audio.ui

import android.os.Bundle
import android.os.Environment
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.ideacode.android_audio.media.mediacodec.VideoDecoder
import com.ideacode.android_audio_record.R

/**
 * Copyright (C), 2021-2025, 无业游民
 * @ProjectName:    AndroidMedia
 * @Package:        com.ideacode.android_audio.ui
 * @ClassName:      MediaCodecActivity
 * @Description:
 * @Author:         randysu
 * @CreateDate:     2025/4/21 00:11
 * @UpdateUser:
 * @UpdateDate:     2025/4/21 00:11
 * @UpdateRemark:
 * @Version:        1.0
 */
class MediaCodecActivity : AppCompatActivity() {

    companion object {
        const val TAG = "MediaCodecActivity"
    }

    lateinit var VIDEO_PATH: String

    lateinit var toolbar: MaterialToolbar
    lateinit var surface_view: SurfaceView

    lateinit var videoDecoder: VideoDecoder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mediacodec)
        toolbar = findViewById(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            this@MediaCodecActivity.finish()
        }

        VIDEO_PATH = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.absolutePath +
                "/VideoDemo/80s_TestVideo.mp4"

        surface_view = findViewById(R.id.surface_view)
        val surfaceHolder = surface_view.holder
        surfaceHolder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                startPlayVideo(holder.surface)
            }

            override fun surfaceChanged(holder: SurfaceHolder, p1: Int, p2: Int, p3: Int) {
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
            }
        })

    }

    fun startPlayVideo(surface : Surface) {
        videoDecoder = VideoDecoder(surface, VIDEO_PATH)
        videoDecoder.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        videoDecoder.release()
    }

}