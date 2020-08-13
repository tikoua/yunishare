package com.example.mycoapplication.utils

import com.tikoua.share.utils.log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.URL
import java.net.URLConnection
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.*

/**
 *   created by dcl
 *   on 2020/8/13 5:47 PM
 */
object DownloadUtils {
    suspend fun download(url: String, path: String): Boolean {
        return withTimeout(30000) {
            withContext(Dispatchers.IO) {
                var success = false
                try {
                    val startTime = System.currentTimeMillis()
                    log("DOWNLOAD", "startTime=$startTime")
                    //下载函数
                    val myURL = URL(url)
                    trustAllHosts()
                    val conn: URLConnection = myURL.openConnection()
                    conn.connect()
                    val `is`: InputStream = conn.getInputStream()
                    val fileSize: Int = conn.getContentLength() //根据响应获取文件大小
                    if (fileSize <= 0) throw RuntimeException("无法获知文件大小 ")
                    val file = File(path)
                    if (file.exists()) {
                        if (file.isFile) {
                            val delete = file.delete()
                            log("delete file: $delete")
                        } else {
                            file.listFiles()?.forEach {
                                val delete = it.delete()
                                log("delete subfile: $delete")
                            }
                            val delete = file.delete()
                            log("delete dir: $delete")

                        }
                    }
                    val parentFile = file.parentFile ?: return@withContext false
                    if (!parentFile.isDirectory) {
                        parentFile.delete()
                    }
                    if (!parentFile.exists()) {
                        parentFile.mkdirs()
                    }
                    File(path)
                    log("path: ${path}  ${file.exists()}  ${file.isDirectory}")
                    //把数据存入路径+文件名
                    val fos = FileOutputStream(path)
                    val buf = ByteArray(1024)
                    var downLoadFileSize = 0
                    do {
                        //循环读取
                        val numread: Int = `is`.read(buf)
                        if (numread == -1) {
                            break
                        }
                        fos.write(buf, 0, numread)
                        downLoadFileSize += numread
                        //更新进度条
                    } while (true)
                    log("DOWNLOAD", "download success")
                    log("DOWNLOAD", "totalTime=" + (System.currentTimeMillis() - startTime))
                    `is`.close()
                    success = true
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    log(tag = "DOWNLOAD", msg = "error: " + ex.message)
                }
                return@withContext success
            }

        }

    }

    fun trustAllHosts() {
        // Create a trust manager that does not validate certificate chains
        // Android use X509 cert
        val trustAllCerts =
            arrayOf<TrustManager>(object : X509TrustManager {
                override fun getAcceptedIssuers(): Array<X509Certificate> {
                    return arrayOf()
                }

                @Throws(CertificateException::class)
                override fun checkClientTrusted(
                    chain: Array<X509Certificate>,
                    authType: String
                ) {
                }

                @Throws(CertificateException::class)
                override fun checkServerTrusted(
                    chain: Array<X509Certificate>,
                    authType: String
                ) {
                }
            })

        // Install the all-trusting trust manager
        try {
            val sc = SSLContext.getInstance("TLS")
            sc.init(null, trustAllCerts, SecureRandom())
            HttpsURLConnection
                .setDefaultSSLSocketFactory(sc.socketFactory)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    val DO_NOT_VERIFY = HostnameVerifier { hostname, session -> true }
}