package com.ideacode.android_audio.ui;

import java.nio.ByteBuffer;

import com.google.android.material.appbar.MaterialToolbar;
import com.ideacode.android_audio_record.R;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.Range;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Copyright (C), 2021-2025, 无业游民
 *
 * @ProjectName: AndroidMedia
 * @Package: com.ideacode.android_audio.ui
 * @ClassName: MediaCodecAsyncActivity
 * @Description:
 * @Author: randysu
 * @CreateDate: 2025/4/27 00:01
 * @UpdateUser:
 * @UpdateDate: 2025/4/27 00:01
 * @UpdateRemark:
 * @Version: 1.0
 */
public class MediaCodecAsyncActivity extends AppCompatActivity {

    private static final String TAG = "MediaCodecAsyncActivity";

    private MaterialToolbar toolbar;
    private Button btn_show_codec_list;
    private Button btn_decode_async;
    private Button btn_decode_sync;
    private SurfaceView surface_media_codec;
    private SurfaceHolder surfaceHolder;

    MediaCodec mediaCodec;
    MediaExtractor mediaExtractor;

    int frameIdx = 0;
    long lastFrameTimeMs = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mediacodec_async);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        btn_show_codec_list = findViewById(R.id.btn_show_codec_list);
        btn_show_codec_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCodecList();
            }
        });
        btn_decode_async = findViewById(R.id.btn_decode_async);
        btn_decode_async.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                codecAsync();
            }
        });

        btn_decode_sync = findViewById(R.id.btn_decode_sync);
        btn_decode_sync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        codecSync();
                    }
                };
                thread.start();
            }
        });
        surface_media_codec = findViewById(R.id.surface_media_codec);
        surfaceHolder = surface_media_codec.getHolder();
    }

    // 显示所有的Codec
    private void showCodecList() {
        MediaCodecList list = new MediaCodecList(MediaCodecList.REGULAR_CODECS);
        MediaCodecInfo[] infos = list.getCodecInfos();

        for (int i = 0; i < infos.length; i++) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Log.i(TAG,
                        i + ":" + infos[i].getName() + ", hw: " + infos[i].isHardwareAccelerated());
                String[] supportTypes = infos[i].getSupportedTypes();
                for (String type : supportTypes) {
                    MediaCodecInfo.CodecCapabilities capabilities =
                            infos[i].getCapabilitiesForType(type);
                    String mime = capabilities.getMimeType();
                    Log.i(TAG, "mime: " + mime);

                    MediaCodecInfo.VideoCapabilities videoCapabilities =
                            capabilities.getVideoCapabilities();
                    if (videoCapabilities != null) {
                        Range<Integer> fpsRange = videoCapabilities.getSupportedFrameRates();
                        Log.i(TAG, "fps: [" + fpsRange.getLower() + ", " + fpsRange.getUpper() +
                                "]");

                        Range<Integer> bitRateRange = videoCapabilities.getBitrateRange();
                        Log.i(TAG,
                                "bitrate: [" + bitRateRange.getLower() + ", " + bitRateRange.getUpper() + "]");
                    }
                }
            }

            Log.i(TAG, "========================");
        }
    }

    private void codecAsync() {
        String fileName = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() +
                "/VideoDemo/80s_TestVideo.mp4";
        Log.e(TAG, "fileName: " + fileName);

        try {
            mediaExtractor = new MediaExtractor();
            mediaExtractor.setDataSource(fileName);

            int selectTrackIndex = 0;
            String mime = "";
            MediaFormat format = null;
            int videoWidth = 0;
            int videoHeight = 0;
            for (int i = 0; i < mediaExtractor.getTrackCount(); i++) {
                selectTrackIndex = i;
                format = mediaExtractor.getTrackFormat(i);
                mime = format.getString(MediaFormat.KEY_MIME);
                Log.i(TAG, "mime: " + mime);
                if (mime.startsWith("video/")) {
                    videoWidth = format.getInteger(MediaFormat.KEY_WIDTH);
                    videoHeight = format.getInteger(MediaFormat.KEY_HEIGHT);
                    break;
                }
            }

            mediaExtractor.selectTrack(selectTrackIndex);
            int frameRate = format.getInteger(MediaFormat.KEY_FRAME_RATE);
            long frameIntervalMs = (long)(1000.0/frameRate);

            Log.i(TAG, "videoWidth: " + videoWidth + "  videoHeight: " + videoHeight);
            float sizeScale = videoWidth * 1f / videoHeight;
            Log.d(TAG, "sizeScale: " + sizeScale);
            LinearLayout.LayoutParams layoutParams =
                    (LinearLayout.LayoutParams)surface_media_codec.getLayoutParams();
            int viewWidth = surface_media_codec.getMeasuredWidth();
            int viewHeight = (int)(viewWidth / sizeScale);
            Log.i(TAG, "viewWidth: " + viewWidth + "  viewHeight: " + viewHeight);
            layoutParams.height = viewHeight;
            surface_media_codec.setLayoutParams(layoutParams);
            surface_media_codec.requestLayout();

            Log.i(TAG, "final mime: " + mime);
            mediaCodec = MediaCodec.createDecoderByType(mime);
            mediaCodec.configure(format, surfaceHolder.getSurface(), null, 0);
            mediaCodec.setCallback(new MediaCodec.Callback() {
                @Override
                public void onInputBufferAvailable(@NonNull MediaCodec mediaCodec, int i) {
                    ByteBuffer buffer = mediaCodec.getInputBuffer(i);
                    int bufferSize = mediaExtractor.readSampleData(buffer, 0);
                    if (frameIdx == -1 || bufferSize <= 0) {
                        mediaCodec.queueInputBuffer(i, 0, 0,0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                        return;
                    }
                    mediaCodec.queueInputBuffer(i, 0, bufferSize,
                            mediaExtractor.getSampleTime() * 1000, 0);
                    mediaExtractor.advance();
                    ++frameIdx;
                }

                @Override
                public void onOutputBufferAvailable(@NonNull MediaCodec mediaCodec, int i,
                                                    @NonNull MediaCodec.BufferInfo bufferInfo) {
                    if (frameIdx == -1 && (bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        return;
                    }

                    long currentTimeMs = System.currentTimeMillis();
                    long delay = frameIntervalMs - (currentTimeMs = lastFrameTimeMs);
                    if (delay > 0) {
                        try {
                            Thread.sleep(delay);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    lastFrameTimeMs = currentTimeMs;
                    mediaCodec.releaseOutputBuffer(i, true);
                }

                @Override
                public void onError(@NonNull MediaCodec mediaCodec,
                                    @NonNull MediaCodec.CodecException e) {

                }

                @Override
                public void onOutputFormatChanged(@NonNull MediaCodec mediaCodec,
                                                  @NonNull MediaFormat mediaFormat) {

                }
            });
            mediaCodec.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 同步解码
    private void codecSync() {
        String fileName = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() +
                "/VideoDemo/80s_TestVideo.mp4";
        Log.e(TAG, "fileName: " + fileName);

        try {
            mediaExtractor = new MediaExtractor();
            mediaExtractor.setDataSource(fileName);

            int selectTrackIndex = 0;
            String mime = "";
            MediaFormat format = null;
            int videoWidth = 0;
            int videoHeight = 0;
            for (int i = 0; i < mediaExtractor.getTrackCount(); i++) {
                selectTrackIndex = i;
                format = mediaExtractor.getTrackFormat(i);
                mime = format.getString(MediaFormat.KEY_MIME);
                Log.i(TAG, "mime: " + mime);
                if (mime.startsWith("video/")) {
                    videoWidth = format.getInteger(MediaFormat.KEY_WIDTH);
                    videoHeight = format.getInteger(MediaFormat.KEY_HEIGHT);
                    break;
                }
            }

            mediaExtractor.selectTrack(selectTrackIndex);
            int frameRate = format.getInteger(MediaFormat.KEY_FRAME_RATE);
            long frameIntervalMs = (long)(1000.0/frameRate);

            Log.i(TAG, "videoWidth: " + videoWidth + "  videoHeight: " + videoHeight);
            float sizeScale = videoWidth * 1f / videoHeight;
            Log.d(TAG, "sizeScale: " + sizeScale);
            LinearLayout.LayoutParams layoutParams =
                    (LinearLayout.LayoutParams)surface_media_codec.getLayoutParams();
            int viewWidth = surface_media_codec.getMeasuredWidth();
            int viewHeight = (int)(viewWidth / sizeScale);
            Log.i(TAG, "viewWidth: " + viewWidth + "  viewHeight: " + viewHeight);
            layoutParams.height = viewHeight;

            runOnUiThread(() -> {
                surface_media_codec.setLayoutParams(layoutParams);
                surface_media_codec.requestLayout();
            });

            Log.i(TAG, "final mime: " + mime);
            mediaCodec = MediaCodec.createDecoderByType(mime);
            mediaCodec.configure(format, surfaceHolder.getSurface(), null, 0);
            mediaCodec.start();

            boolean isEof = false;
            while (!isEof) {
                // Input
                int inputIdx = -1;
                if ((inputIdx = mediaCodec.dequeueInputBuffer(-1)) >= 0) {
                    ByteBuffer buffer = mediaCodec.getInputBuffer(inputIdx);
                    int sampleSize = mediaExtractor.readSampleData(buffer, 0);
                    Log.d(TAG, "sampleSize: " + sampleSize);
                    if (sampleSize > 0) {
                        mediaCodec.queueInputBuffer(inputIdx, 0, sampleSize,
                                mediaExtractor.getSampleTime() * 1000, mediaExtractor.getSampleFlags());
                        mediaExtractor.advance();
                    } else {
                        mediaCodec.queueInputBuffer(inputIdx, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                    }
                }

                // Output
                MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
                int outputIdx = -1;
                // 设置超时时间
                outputIdx = mediaCodec.dequeueOutputBuffer(info, 10000);
                if (outputIdx >= 0) {
                    long currentTimeMs = System.currentTimeMillis();
                    long delay = frameIntervalMs - (currentTimeMs - lastFrameTimeMs);
                    if (delay > 0) {
                        Thread.sleep(delay);
                    }
                    lastFrameTimeMs = currentTimeMs;
                    mediaCodec.releaseOutputBuffer(outputIdx, true);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mediaCodec != null) {
            mediaCodec.stop();
            mediaCodec.release();
        }

        if (mediaExtractor != null) {
            mediaExtractor.release();
        }
    }
}
