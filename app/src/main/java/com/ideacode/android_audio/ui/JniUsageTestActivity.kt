/*
 * Copyright (C) 2025 Baidu, Inc. All Rights Reserved.
 */
package com.ideacode.android_audio.ui

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.ideacode.android_audio_record.R

/**
 * Copyright (C), 2021-2025, 无业游民
 * @ProjectName:    AndroidMedia
 * @Package:        com.ideacode.android_audio.ui
 * @ClassName:      JniUsageTest
 * @Description:
 * @Author:         randysu
 * @CreateDate:     2025/5/1 23:57
 * @UpdateUser:
 * @UpdateDate:     2025/5/1 23:57
 * @UpdateRemark:
 * @Version:        1.0
 */
class JniUsageTestActivity : AppCompatActivity() {

    companion object {
        const val TAG = "JniUsageTestActivity"
        init {
            // 最后加载自己的 JNI 库
            System.loadLibrary("native-lib")
        }
    }

    lateinit var toolbar: MaterialToolbar
    lateinit var jni_result: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_jniusagetest)

        toolbar = findViewById(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        jni_result = findViewById(R.id.jni_result)

        var sb = StringBuilder()
        sb.append(stringFromJNI())
        jni_result.setText(sb.toString())
    }

    external fun stringFromJNI(): String

}