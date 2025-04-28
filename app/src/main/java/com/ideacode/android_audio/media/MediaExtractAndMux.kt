/*
 * Copyright (C) 2025 Baidu, Inc. All Rights Reserved.
 */
package com.ideacode.android_audio.media

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.nio.ByteBuffer

/**
 * Copyright (C), 2021-2025, 无业游民
 * @ProjectName:    AndroidMedia
 * @Package:        com.ideacode.android_audio.media
 * @ClassName:      MediaExtraAndMux
 * @Description:
 * @Author:         randysu
 * @CreateDate:     2025/4/20 17:47
 * @UpdateUser:
 * @UpdateDate:     2025/4/20 17:47
 * @UpdateRemark:
 * @Version:        1.0
 */
object MediaExtractAndMux {

    fun extractAndMuxVideo(inputPath: String, outputPath: String) {
        CoroutineScope(Dispatchers.IO).launch {
            Log.d("Mux", "开始处理，inputpath: $inputPath  outputPath: $outputPath")

            val extractor = MediaExtractor()
            val muxer: MediaMuxer

            try {
                extractor.setDataSource(inputPath)

                muxer = MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)

                var videoTrackIndex = -1
                var audioTrackIndex = -1
                var newVideoTezckIndex = -1
                var newAudioTrackIndex = -1

                for (i in 0 until extractor.trackCount) {
                    val format = extractor.getTrackFormat(i)
                    val mime = format.getString(MediaFormat.KEY_MIME)

                    if (mime?.startsWith("video/") == true) {
                        videoTrackIndex = i
                        newVideoTezckIndex = muxer.addTrack(format)
                    } else if (mime?.startsWith("audio/") == true) {
                        audioTrackIndex = i
                        newAudioTrackIndex = muxer.addTrack(format)
                    }
                }

                muxer.start()

                val bufferSize = 1 * 1014 * 1024
                val buffer = ByteBuffer.allocate(bufferSize)
                val bufferInfo = MediaCodec.BufferInfo()

                if (videoTrackIndex != -1) {
                    extractor.selectTrack(videoTrackIndex)
                    while (true) {
                        buffer.clear()
                        val sampleSize = extractor.readSampleData(buffer, 0)
                        if (sampleSize < 0) {
                            break
                        }

                        bufferInfo.offset = 0
                        bufferInfo.size = sampleSize
                        bufferInfo.presentationTimeUs = extractor.sampleTime
                        bufferInfo.flags = extractor.sampleFlags

                        muxer.writeSampleData(newVideoTezckIndex, buffer, bufferInfo)
                        extractor.advance()
                    }
                    extractor.unselectTrack(videoTrackIndex)
                }

                if (audioTrackIndex != -1) {
                    extractor.selectTrack(audioTrackIndex)
                    while (true) {
                        buffer.clear()
                        val sampleSize = extractor.readSampleData(buffer, 0)
                        if (sampleSize < 0) {
                            break
                        }

                        bufferInfo.offset = 0
                        bufferInfo.size = sampleSize
                        bufferInfo.presentationTimeUs = extractor.sampleTime
                        bufferInfo.flags = extractor.sampleFlags

                        muxer.writeSampleData(newAudioTrackIndex, buffer, bufferInfo)
                        extractor.advance()
                    }
                    extractor.unselectTrack(audioTrackIndex)
                }

                muxer.stop()
                muxer.release()
                extractor.release()

                Log.d("Mux", "处理完成，保存为: $outputPath")

            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

}