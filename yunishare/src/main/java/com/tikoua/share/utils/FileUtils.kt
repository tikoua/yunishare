package com.tikoua.share.utils

import android.content.Context

/**
 *   created by dcl
 *   on 2020/9/1 17:44
 */
object FileUtils {
    fun copyToShareTemp(context: Context, source: String): String? {
        /*val dir = File(context.getExternalFilesDir(null), "share/temp")
        if (!dir.exists()) {
            dir.mkdirs()
        } else {
            dir.listFiles()?.forEach {
                it.delete()
            }
        }
        val sourceFile = File(source)
        var name = sourceFile.name
        val hasSuffix = name.endsWith(".jpg", true) ||
                name.endsWith(".jpeg", true) ||
                name.endsWith(".png", true) ||
                name.endsWith(".mp4", true)

        if (!hasSuffix && !name.contains(".")) {
            val fileType = MimeTypeUtils.getFileType(source)
            if (!fileType.isNullOrEmpty()) {
                name = "${name}.${fileType}"
            }
        }
        val file = File(dir, name)
        return try {
            sourceFile.copyTo(file, true)
            file.absolutePath
        } catch (error: Throwable) {
            error.printStackTrace()
            null
        }*/
        return source
    }
}