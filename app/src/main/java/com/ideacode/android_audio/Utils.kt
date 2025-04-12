/*
 * Copyright (C) 2025 Baidu, Inc. All Rights Reserved.
 */
package com.ideacode.android_audio

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import java.io.Closeable
import java.io.File
import java.io.IOException

/**
 * Copyright (C), 2021-2025, 无业游民
 * @ProjectName:    AndroidMedia01
 * @Package:        com.ideacode.android_audio
 * @ClassName:      Utils
 * @Description:
 * @Author:         randysu
 * @CreateDate:     2025/4/12 15:14
 * @UpdateUser:
 * @UpdateDate:     2025/4/12 15:14
 * @UpdateRemark:
 * @Version:        1.0
 */
object Utils {
    fun checkPermissionIsGranted(context: Context?, permission: String?): Boolean {
        if (context == null || permission == null) {
            return false
        }
        return ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED
    }

    fun close(closeables : MutableList<Closeable>) {
        if (closeables.isNullOrEmpty()) {
            closeables.forEach {
                it.close()
            }
        }
    }

    fun getFile(path: String, name: String): File {
        var file = File(path, name)
        if (file.exists()) {
            file.delete()
        }
        try {
            file.createNewFile()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return file
    }
}
