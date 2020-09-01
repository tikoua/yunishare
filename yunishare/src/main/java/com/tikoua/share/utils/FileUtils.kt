package com.tikoua.share.utils

import android.content.Context
import android.os.FileUtils
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 *   created by dcl
 *   on 2020/9/1 17:44
 */
object FileUtils {
    fun copyToShareTemp(context: Context, source: String): String? {
        val dir = File(context.cacheDir, "share/temp")
        if (!dir.exists()) {
            dir.mkdirs()
        } else {
            dir.listFiles()?.forEach {
                it.delete()
            }
        }
        val sourceFile = File(source)
        val file = File(dir, sourceFile.name)
        return try {
            FileUtils.copy(FileInputStream(source), FileOutputStream(file))
            file.absolutePath
        } catch (error: Throwable) {
            null
        }
    }
}