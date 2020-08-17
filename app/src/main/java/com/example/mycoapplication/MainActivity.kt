package com.example.mycoapplication

import android.net.Uri
import android.os.Bundle
import android.webkit.MimeTypeMap
import androidx.appcompat.app.AppCompatActivity
import com.example.mycoapplication.utils.DownloadUtils
import com.tikoua.share.YuniShare
import com.tikoua.share.model.InnerShareParams
import com.tikoua.share.model.ShareChannel
import com.tikoua.share.utils.log
import com.tikoua.share.wechat.loadWechatMeta
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import java.io.File
import java.io.FileInputStream


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val text = "分享的文本"
        val imageUrl = "https://cdn.uneed.com/download/box_string200810.jpg"
        val videoUrl =
            "https://cdn.uneed.com/download/0bf2oeaaiaaa4yaob6igj5pva4odaryqabaa.f10003.mp4"
        val pageUrl = "https://www.rockmessenger.com/index.html"
        val path = "pages/index/main?uid=124287543079337984&key=491209839083520000"
        val title = "海非深64邀请你成为好友"
        val miniCover = "https://cdn.uneed.com/web/icon/add_friends.png"
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        GlobalScope.launch {
            repeat(100) {
                pv.setProgress(it + 1)
                delay(500)
            }
        }
        bt.setOnClickListener {
            testExten()
        }
        btClear.setOnClickListener {
            edt.setText(null)
            tvResult.text = null
        }
        btWechatFriendText.setOnClickListener {
            GlobalScope.launch {
                testShareWechatFriendText(text)
            }
        }
        btWechatFriendImage.setOnClickListener {
            GlobalScope.launch {
                testShareWechatFriendImage(imageUrl)
            }
        }
        btWechatFriendVideo.setOnClickListener {
            GlobalScope.launch {
                testShareWechatFriendVideo(videoUrl)
            }
        }
        btWechatFriendMiniProgram.setOnClickListener {
            GlobalScope.launch {
                testShareWechatFriendMiniprogram(pageUrl, path, title, miniCover)
            }
        }
        btWechatMomentText.setOnClickListener {
            GlobalScope.launch {
                testShareWechatMomentText(text)
            }
        }
        btWechatMomentImage.setOnClickListener {
            GlobalScope.launch {
                testShareWechatMomentImage(imageUrl)
            }
        }
        btWechatMomentVideo.setOnClickListener {
            GlobalScope.launch {
                testShareWechatMomentVideo(videoUrl)
            }
        }
        btWechatMomentMiniProgram.setOnClickListener {
            GlobalScope.launch {
                testShareWechatMomentMiniProgram(pageUrl, path, title, miniCover)
            }
        }
        btQQText.setOnClickListener {
            GlobalScope.launch {
                testShareQQText(text)
            }
        }
        btQQLocalImage.setOnClickListener {
            GlobalScope.launch {
                testShareQQImage(imageUrl)
            }
        }
        btQQRemoteImage.setOnClickListener {
            GlobalScope.launch {
                testShareQQRemoteImage(imageUrl)
            }
        }
        btQQVideo.setOnClickListener {
            GlobalScope.launch {
                testShareQQRemoteVideo(videoUrl)
            }
        }

        YuniShare.init(this)
    }


    private fun testExten() {
        val text = edt.text.toString()
        val encode1 = Uri.encode(text, "/:")
        val fileExtensionFromUrl =
            MimeTypeMap.getFileExtensionFromUrl(encode1)
        tvResult.setText(fileExtensionFromUrl)
    }

    private suspend fun testShareWechatFriendText(text: String) {
        YuniShare.share(
            this,
            ShareChannel.WechatFriend,
            InnerShareParams.buildWechatText().text(text).build()
        ).apply {
            log("share result: $this")
        }

    }

    private suspend fun testShareWechatFriendImage(imageUrl: String) {
        log("testShareImage 1")
        val imagePath = getFilePath("save/img/hahah.jpg", imageUrl)

        log("testShareImage 2  imagePath: $imagePath")
        if (imagePath.isNullOrEmpty()) {
            return
        }
        log("imagePath: $imagePath")
        YuniShare.share(
            this@MainActivity,
            ShareChannel.WechatFriend,
            InnerShareParams.buildWechatImage().title("分享图片").imagePath(imagePath).build()
        ).apply {
            log("share result: $this")
        }
    }

    private suspend fun testShareWechatFriendVideo(videoUrl: String) {
        log("testShareWechatFriendVideo: $videoUrl")
        YuniShare.share(
            this@MainActivity,
            ShareChannel.WechatFriend,
            InnerShareParams.buildWechatVideo().videoUrl(videoUrl).title("标题: 与你分享来的视频")
                .desc("描述: 狗咬狗哈哈哈").build()
        ).apply {
            log("share result: $this")
        }
    }

    private suspend fun testShareWechatFriendMiniprogram(
        pageUrl: String,
        path: String,
        title: String,
        miniCover: String
    ) {
        val filePath = getFilePath("save/img/thumbcover.jpg", miniCover)
        val thumb = filePath?.let {
            withContext(Dispatchers.IO) {
                return@withContext FileInputStream(filePath).use { fis ->
                    val bytes = ByteArray(fis.available())
                    fis.read(bytes)
                    return@use bytes
                }
            }
        }
        log("thumb: ${thumb?.size}")
        YuniShare.share(
            this,
            ShareChannel.WechatFriend,
            InnerShareParams.buildMiniProgram()
                .path(path)
                .webPageUrl(pageUrl)
                .thumbData(thumb)
                .userName(loadWechatMeta().userName)
                .title("小程序标题")
                .build()
        ).apply {
            log("share result: $this")
        }

    }


    private suspend fun testShareWechatMomentText(text: String) {
        YuniShare.share(
            this,
            ShareChannel.WechatMoment,
            InnerShareParams.buildWechatText().text(text).build()
        ).apply {
            log("share result: $this")
        }
    }

    private suspend fun testShareWechatMomentImage(imageUrl: String) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                log("testShareImage 1")
                val imagePath = getFilePath("save/img/hahha.jpg", imageUrl)

                log("testShareImage 2  imagePath: $imagePath")
                if (imagePath.isNullOrEmpty()) {
                    return@launch
                }
                log("imagePath: $imagePath")
                YuniShare.share(
                    this@MainActivity,
                    ShareChannel.WechatMoment,
                    InnerShareParams.buildWechatImage().title("分享图片").imagePath(imagePath).build()
                ).apply {
                    log("share result: $this")
                }
            } catch (error: Throwable) {
                error.printStackTrace()
            }
        }
    }

    private suspend fun testShareWechatMomentVideo(videoUrl: String) {
        log("testShareWechatFriendVideo: $videoUrl")
        YuniShare.share(
            this@MainActivity,
            ShareChannel.WechatMoment,
            InnerShareParams.buildWechatVideo().videoUrl(videoUrl).title("标题: 与你分享来的视频")
                .desc("描述: 狗咬狗哈哈哈").build()
        ).apply {
            log("share result: $this")
        }
    }

    private suspend fun testShareWechatMomentMiniProgram(
        pageUrl: String,
        path: String,
        title: String,
        miniCover: String
    ) {
        val filePath = getFilePath("save/img/thumbcover.jpg", miniCover)
        val thumb = filePath?.let {
            withContext(Dispatchers.IO) {
                return@withContext FileInputStream(filePath).use { fis ->
                    val bytes = ByteArray(fis.available())
                    fis.read(bytes)
                    return@use bytes
                }
            }
        }
        log("thumb: ${thumb?.size}")
        YuniShare.share(
            this,
            ShareChannel.WechatMoment,
            InnerShareParams.buildMiniProgram()
                .path(path)
                .webPageUrl(pageUrl)
                .thumbData(thumb)
                .userName(loadWechatMeta().userName)
                .title(title)
                .build()
        ).apply {
            log("share result: $this")
        }

    }

    private suspend fun testShareQQText(text: String) {
        YuniShare.share(
            this,
            ShareChannel.QQFriend,
            InnerShareParams.buildQQText().text(text)
                .targetUrl("https://www.baidu.com").build()
        ).apply {
            log("share result: $this")
        }
    }

    private suspend fun testShareQQImage(imageUrl: String) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                log("testShareImage 1")
                val imagePath = getFilePath("save/img/hahha.jpg", imageUrl)

                log("testShareImage 2  imagePath: $imagePath")
                if (imagePath.isNullOrEmpty()) {
                    return@launch
                }
                log("imagePath: $imagePath")
                YuniShare.share(
                    this@MainActivity,
                    ShareChannel.QQFriend,
                    InnerShareParams.buildQQImage().imagePath(imagePath).build()
                ).apply {
                    log("share result: $this")
                }
            } catch (error: Throwable) {
                error.printStackTrace()
            }
        }
    }

    private suspend fun testShareQQRemoteImage(imageUrl: String) {
        YuniShare.share(
            this@MainActivity,
            ShareChannel.QQFriend,
            InnerShareParams.buildQQImage().imageUrl(imageUrl).title("图片标题").desc("图片描述")
                .appName("与你").build()
        ).apply {
            log("share result: $this")
        }
    }

    private suspend fun testShareQQRemoteVideo(imageUrl: String) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                log("testShareQQRemoteVideo 1")
                val imagePath = getFilePath("save/img/hahha.mp4", imageUrl)

                log("testShareQQRemoteVideo 2  imagePath: $imagePath")
                if (imagePath.isNullOrEmpty()) {
                    return@launch
                }
                log("testShareQQRemoteVideo: $imagePath")
                YuniShare.share(
                    this@MainActivity,
                    ShareChannel.QQFriend,
                    InnerShareParams.buildQQImage().imagePath(imagePath)
                        .appName("与你").build()
                ).apply {
                    log("share result: $this")
                }
            } catch (error: Throwable) {
                error.printStackTrace()
            }
        }

    }

    private suspend fun downloadFile(path: String, url: String): String? {
        val download = DownloadUtils.download(url, path)
        return if (download) path else null
    }

    private suspend fun getFilePath(subPath: String, url: String): String? {
        val filesDir = externalCacheDir
        val file = File(filesDir, subPath)
        if (file.exists() && file.isFile) {
            return file.absolutePath
        }
        return downloadFile(file.absolutePath, url)
    }


}
