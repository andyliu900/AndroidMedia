/*
 * Copyright (C) 2025 Baidu, Inc. All Rights Reserved.
 */
package com.ideacode.android_audio.ui

import android.media.MediaExtractor
import android.media.MediaFormat
import android.os.Bundle
import android.os.Environment
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.ideacode.android_audio.media.MediaExtractAndMux
import com.ideacode.android_audio_record.R
import java.io.File

/**
 * Copyright (C), 2021-2025, 无业游民
 * @ProjectName:    AndroidMedia
 * @Package:        com.ideacode.android_audio.ui
 * @ClassName:      ExtractorMuxerActivity
 * @Description:
 * @Author:         randysu
 * @CreateDate:     2025/4/13 22:10
 * @UpdateUser:
 * @UpdateDate:     2025/4/13 22:10
 * @UpdateRemark:
 * @Version:        1.0
 */
class ExtractorMuxerActivity : AppCompatActivity() {

    companion object {
        const val TAG = "ExtractorMuxerActivity"
    }

    lateinit var ROOT_PATH: String
    lateinit var ORIGIN_VIDEO_FILENAME: String
    lateinit var NEW_VIDEO_FILENAME: String
    var mVideoTrackId: Int = 0
    var mVideoFormat: MediaFormat? = null
    var mAudiotrackId: Int = 0
    var mAudioFormat: MediaFormat? = null

    lateinit var toolbar: MaterialToolbar
    lateinit var video_info: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_extractor)
        toolbar = findViewById(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            this@ExtractorMuxerActivity.finish()
        }

        video_info = findViewById(R.id.video_info)

        ROOT_PATH = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.absolutePath + "/VideoDemo"
        ORIGIN_VIDEO_FILENAME = "80s_TestVideo.mp4"
        NEW_VIDEO_FILENAME = "new_video.mp4"
        getMediaInfo()

        // 解析视频，再合成新视频
        MediaExtractAndMux.extractAndMuxVideo(ROOT_PATH + File.separator + ORIGIN_VIDEO_FILENAME,
            ROOT_PATH + File.separator + NEW_VIDEO_FILENAME)

    }

    fun getMediaInfo() {
        var sb = StringBuilder()
        val file = File(ROOT_PATH + File.separator + ORIGIN_VIDEO_FILENAME)

        val mediaExtractor = MediaExtractor()
        sb.append("视频名称： ${file.name}").append("\n")
        mediaExtractor.setDataSource(ROOT_PATH + File.separator + ORIGIN_VIDEO_FILENAME)
        val count = mediaExtractor.trackCount
        sb.append("视频轨道数：${count}  ").append("\n")
        for (i in 0 until  count) {
            val format = mediaExtractor.getTrackFormat(i)
            // 获取 mime
            format.getString(MediaFormat.KEY_MIME)?.let {mime ->
                sb.append(mime).append("\n")
                // 视频轨
                if (mime.startsWith("video")) {
                    mVideoTrackId = i
                    mVideoFormat = format
                } else if (mime.startsWith("audio")) {
                    mAudiotrackId = i
                    mAudioFormat = format
                }
            }
        }

        mVideoFormat?.let {mVideoFormat ->
            val width = mVideoFormat.getInteger(MediaFormat.KEY_WIDTH)
            val height = mVideoFormat.getInteger(MediaFormat.KEY_HEIGHT)
            sb.append("视频宽高：${width} x ${height}").append("\n")

            val aLong = mVideoFormat.getLong(MediaFormat.KEY_DURATION)
            sb.append("视频播放总时长： ${(aLong / 1000 / 1000 / 60)}分钟").append("\n")

            val frameRate = mVideoFormat.getInteger(MediaFormat.KEY_FRAME_RATE)
            sb.append("视频帧率： ${frameRate}fps").append("\n")
        }

        mAudioFormat?.let { mAudioFormat ->
            val aLong = mAudioFormat.getLong(MediaFormat.KEY_DURATION)
            sb.append("音频时长：${(aLong / 1000 / 1000 / 60)}分钟").append("\n")

            val audioRate = mAudioFormat.getInteger(MediaFormat.KEY_BIT_RATE)
            sb.append("音频码率：${audioRate}").append("\n")
        }

        video_info.setText(sb.toString())
    }



}