package com.tikoua.share.utils

import java.net.HttpURLConnection
import java.net.URL

/**
 *   created by dcl
 *   on 2020/8/20 6:32 PM
 */
object UrlUtils {
    private const val Max = 1024 * 1024 * 1024

    /**
     * 用于确定是很小的文件的获取
     */
    fun get(url: String): ByteArray? {
        var conn: HttpURLConnection? = null
        try {
            val localUrl = URL(url)
            conn = localUrl.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.doInput = true
            conn.connect()
            val contentLength = conn.contentLength
            val data = conn.inputStream.use {
                if (contentLength > Max) {
                    return@use null
                }
                val bytes = ByteArray(contentLength)
                it.read(bytes)
                bytes
            }
            return data
        } catch (error: Throwable) {
            error.printStackTrace()
        } finally {
            try {
                conn?.disconnect()
            } catch (error: Throwable) {
                error.printStackTrace()
            }
        }
        return null
    }

}