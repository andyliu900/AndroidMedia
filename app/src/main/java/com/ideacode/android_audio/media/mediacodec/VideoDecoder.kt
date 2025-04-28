/*
 * Copyright (C) 2025 Baidu, Inc. All Rights Reserved.
 */
package com.ideacode.android_audio.media.mediacodec

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.view.Surface
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.IllegalStateException

/**
 * Copyright (C), 2021-2025, 无业游民
 * @ProjectName:    AndroidMedia
 * @Package:        com.ideacode.android_audio.media.mediacodec
 * @ClassName:      VideoDecoder
 * @Description:
 * @Author:         randysu
 * @CreateDate:     2025/4/20 23:52
 * @UpdateUser:
 * @UpdateDate:     2025/4/20 23:52
 * @UpdateRemark:
 * @Version:        1.0
 */
class VideoDecoder(val surface: Surface, val videoPath: String) {

    var mediaExtractor: MediaExtractor? = null
    var mediaCodec: MediaCodec? = null
    var isRunning = false

    fun start() {
        mediaExtractor = MediaExtractor().apply {
            setDataSource(videoPath)
        }

        // 查找视频轨道
        val videoTrackIndex = (0 until mediaExtractor!!.trackCount)
            .firstOrNull { i ->
                mediaExtractor!!.getTrackFormat(i).getString(MediaFormat.KEY_MIME)?.startsWith("video/") ?: false
            } ?: throw IllegalStateException("No video track found")

        mediaExtractor!!.selectTrack(videoTrackIndex)

        val videoFormat = mediaExtractor!!.getTrackFormat(videoTrackIndex)

        // 初始化解码器
        mediaCodec = MediaCodec.createDecoderByType(videoFormat.getString(MediaFormat.KEY_MIME)
        !!).apply {
            configure(videoFormat, surface, null, 0)
            start()
        }

        isRunning = true

        CoroutineScope(Dispatchers.IO).launch {
            decodeLoop()
        }
    }

    suspend fun decodeLoop() {
        val codec = mediaCodec ?: return
        val extractor = mediaExtractor ?: return

        val bufferInfo = MediaCodec.BufferInfo()

        var startTime = System.nanoTime()

        while (isRunning) {
            // 提交数据到解码器输入缓冲区
            val inputBufferId = codec.dequeueInputBuffer(1000)
            if (inputBufferId >= 0) {
                val inputBuffer = codec.getInputBuffer(inputBufferId)!!
                val sampleSize = extractor.readSampleData(inputBuffer, 0)

                if (sampleSize >= 0) {
                    codec.queueInputBuffer(
                        inputBufferId,
                        0,
                        sampleSize,
                        extractor.sampleTime,
                        extractor.sampleFlags
                    )
                    extractor.advance()
                } else {
                    // 输入结束
                    codec.queueInputBuffer(
                        inputBufferId,
                        0,
                        0,
                        0,
                        MediaCodec.BUFFER_FLAG_END_OF_STREAM
                    )
                }
            }

            // 处理解码器输出
            val outputBufferId = codec.dequeueOutputBuffer(bufferInfo, 1000)
            when {
                outputBufferId >= 0 -> {
                    // 渲染到 Surface
                    val ptsUs = bufferInfo.presentationTimeUs
                    val nowUs = (System.nanoTime() - startTime) / 1000
                    if (ptsUs > nowUs) {
                        delay((ptsUs - nowUs)/1000)
                    }

                    codec.releaseOutputBuffer(outputBufferId, true)
                }
                outputBufferId == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                    // 格式变化（通常首次回调）
                    val newFormat = codec.outputFormat
                }
            }

            // 检查结束标志
            if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                break
            }
        }

        release()
    }

    fun release() {
        isRunning = false
        mediaCodec?.stop()
        mediaCodec?.release()
        mediaExtractor?.release()
    }



}