/*
 * Copyright (C) 2025 Baidu, Inc. All Rights Reserved.
 */
package com.ideacode.android_audio.ui

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.ideacode.android_audio_record.R
import com.ideacode.android_audio.Utils
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.AudioTrack
import android.util.Log
import android.view.MotionEvent
import android.widget.Button
import androidx.core.app.ActivityCompat
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.RandomAccessFile

/**
 * Copyright (C), 2021-2025, 无业游民
 * @ProjectName:    AndroidMedia
 * @Package:        com.ideacode.android_audio_record.ui
 * @ClassName:      AudioRecordActivity
 * @Description:
 * @Author:         randysu
 * @CreateDate:     2025/4/12 12:07
 * @UpdateUser:
 * @UpdateDate:     2025/4/12 12:07
 * @UpdateRemark:
 * @Version:        1.0
 */

@SuppressLint("MissingPermission")
class AudioRecordActivity : AppCompatActivity() {

    companion object {
        const val TAG: String = "AudioRecordActivity"
        const val AUDIO_RATE = 44100
    }

    var mAudioThread: AudioThread? = null
    var mAudioTrackThread: AudioTrackThread? = null
    lateinit var PATH: String

    lateinit var toolbar: MaterialToolbar
    lateinit var audio_record: Button
    lateinit var play_pcm: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_record)

        toolbar = findViewById(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            this@AudioRecordActivity.finish()
        }

        audio_record = findViewById(R.id.audio_record)
        audio_record.setOnTouchListener { view, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                startRecord()
            } else if (motionEvent.action ==  MotionEvent.ACTION_UP
                || motionEvent.action == MotionEvent.ACTION_CANCEL) {
                mAudioThread?.let {
                    mAudioThread!!.done()
                }
            }

            return@setOnTouchListener false
        }

        play_pcm = findViewById(R.id.play_pcm)
        play_pcm.setOnClickListener {
            playPcm()
        }

        PATH = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.absolutePath + "/VideoDemo"
    }

    private fun startRecord() {
        mAudioThread?.let { mAudioThread?.done() }

        // 开始录制
        mAudioThread = AudioThread(PATH)
        mAudioThread?.start()

        Log.d(TAG, "开始录制音频")
    }

    // 音频录制线程
    class AudioThread(path: String) : Thread() {
        val PATH: String

        lateinit var record: AudioRecord
        var minBufferSize: Int = 0
        var isDone: Boolean = false

        init {
            PATH = path

            /**
             * 获取最小 buffer 大小
             * 采样率 44100，双声道，采样位数为 16bit
             */
            minBufferSize = AudioRecord.getMinBufferSize(
                AUDIO_RATE,
                AudioFormat.CHANNEL_IN_STEREO,
                AudioFormat.ENCODING_PCM_16BIT
            )
        }

        override fun run() {
            super.run()

            try {
                // 先创建文件夹
                var dir = File(PATH)
                if (!dir.exists()) {
                    val result = dir.mkdirs()
                    Log.d(TAG, "create path result: ${result}")
                }

                // 创建 pcm 文件
                val pcmFile = Utils.getFile(PATH, "test.pcm")
                Log.d(TAG, "pcmFile: ${pcmFile.isFile}")

                /**
                 * 使用 AudioRecord 去录音
                 */
                if (!::record.isInitialized) {
                    record = AudioRecord(
                        MediaRecorder.AudioSource.MIC,
                        AUDIO_RATE,
                        AudioFormat.CHANNEL_IN_STEREO,
                        AudioFormat.ENCODING_PCM_16BIT,
                        minBufferSize)
                    Log.d(TAG, "初始化 record ${record}")
                }
                record.startRecording()

                var buffer = ByteArray(size = minBufferSize)

                FileOutputStream(pcmFile).use { outputStream ->
                    while (!isDone) {
                        val read = record.read(buffer, 0, buffer.size)
                        Log.d(TAG, "音频数据 ${read}")

                        if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                            outputStream.write(buffer, 0, read)
                        }
                    }
                }

                // 录制结束
                record.stop()
                record.release()
            } catch (e: IOException) {
                e.printStackTrace()
                Log.d(TAG, "audio record exception: ${e.message}")
            }
        }

        fun done() {
            interrupt()
            isDone = true

            Log.d(TAG, "停止录制音频")
        }
    }

    fun playPcm() {
        mAudioTrackThread = AudioTrackThread(PATH)
        mAudioTrackThread?.start()
    }

    class AudioTrackThread(path: String) : Thread() {

        val PATH: String
        var audioTrack: AudioTrack
        val bufferSize: Int
        var isDone: Boolean = false

        init {
            PATH = path

            val channelConfig = AudioFormat.CHANNEL_IN_STEREO

            /**
             * 设置音频信息属性
             * 1.设置支持多媒体属性，如 audio、video
             * 2.设置音频格式，如 music
             */
            val attributes: AudioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()

            /**
             * 设置音频特色
             * 1.设置采样率
             * 2.设置采样位数
             * 3.设置声道
             */
            val format: AudioFormat = AudioFormat.Builder()
                .setSampleRate(AUDIO_RATE)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setChannelMask(channelConfig)
                .build()

            bufferSize = AudioTrack.getMinBufferSize(AUDIO_RATE, channelConfig, AudioFormat.ENCODING_PCM_16BIT)

            audioTrack = AudioTrack(
                attributes,
                format,
                bufferSize,
                AudioTrack.MODE_STREAM,
                AudioManager.AUDIO_SESSION_ID_GENERATE
            )

            audioTrack.play()
        }

        override fun run() {
            super.run()
            val pcmFile = File(PATH, "test.pcm")
            Log.d(TAG, "pcm 文件是否存在：${pcmFile.exists()} isDone: ${isDone}")
            if (pcmFile.exists()) {

                FileInputStream(pcmFile).use { inputStream ->
                    val buffer = ByteArray(size = bufferSize)
                    var bytesRead = 0

                    while (!isDone && inputStream.read(buffer).also { bytesRead = it } != -1) {
                        Log.d(TAG, "pcm 数据：${bytesRead}")
                        audioTrack.write(buffer, 0, bytesRead)
                    }
                }

                audioTrack.stop()
                audioTrack.release()
            }
        }

        fun done() {
            isDone = true
        }
    }

}