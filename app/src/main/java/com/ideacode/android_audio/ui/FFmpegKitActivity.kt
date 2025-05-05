/*
 * Copyright (C) 2025 Baidu, Inc. All Rights Reserved.
 */
package com.ideacode.android_audio.ui

import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.FFmpegSession
import com.google.android.material.appbar.MaterialToolbar
import com.ideacode.android_audio_record.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File


/**
 * Copyright (C), 2021-2025, 无业游民
 * @ProjectName:    AndroidMedia
 * @Package:        com.ideacode.android_audio.ui
 * @ClassName:      FFmpegKitActivity
 * @Description:
 * @Author:         randysu
 * @CreateDate:     2025/5/5 22:59
 * @UpdateUser:
 * @UpdateDate:     2025/5/5 22:59
 * @UpdateRemark:
 * @Version:        1.0
 */
class FFmpegKitActivity : AppCompatActivity() {

    companion object {
        const val TAG = "FFmpegKitActivity"
    }

    lateinit var ROOT_PATH: String
    lateinit var ORIGIN_VIDEO_FILENAME: String
    lateinit var NEW_VIDEO_FILENAME: String

    lateinit var toolbar: MaterialToolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ffmpegkit)

        toolbar = findViewById(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        ROOT_PATH = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.absolutePath + "/VideoDemo"
        ORIGIN_VIDEO_FILENAME = "80s_TestVideo.mp4"
        NEW_VIDEO_FILENAME = "new_video.mp4"
        executeFFmpegCmd("-i ${ROOT_PATH + File.separator + ORIGIN_VIDEO_FILENAME} -s 320x240 ${ROOT_PATH + File.separator + NEW_VIDEO_FILENAME}")
    }

    fun executeFFmpegCmd(cmd: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val session = FFmpegKit.execute(cmd)

            val state = session.state
            val returnCode = session.returnCode

            Log.d(TAG, "FFmpeg process exited with state $state and rc $returnCode")
        }
    }

}