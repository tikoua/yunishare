package com.example.mycoapplication

import android.net.Uri
import android.os.Bundle
import android.os.Environment.DIRECTORY_PICTURES
import android.webkit.MimeTypeMap
import androidx.appcompat.app.AppCompatActivity
import com.example.mycoapplication.utils.DownloadUtils
import com.tikoua.share.YuniShare
import com.tikoua.share.model.InnerShareParams
import com.tikoua.share.model.ShareChannel
import com.tikoua.share.utils.log
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val text = "分享的文本"
        val imageUrl = "https://cdn.uneed.com/download/box_string200810.jpg"
        val videoUrl =
            "https://cdn.uneed.com/download/0bf2oeaaiaaa4yaob6igj5pva4odaryqabaa.f10003.mp4"
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
                testShareWechatMomentVideo()
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
        val imagePath = getImagePath(imageUrl)

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
                val imagePath = getImagePath(imageUrl)

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

    private suspend fun testShareWechatMomentVideo() {

    }

    private suspend fun getImagePath(imageUrl: String): String? {
        log("getImagePath 1")
        val path = "svae/test/textimg.jpg"
        val externalFilesDir = getExternalFilesDir(DIRECTORY_PICTURES)
        val file = File(externalFilesDir, path)
        if (file.exists() && file.isFile) {
            log("getImagePath 2")

            return file.absolutePath
        }
        log("getImagePath 3")
        return downloadFile(file.absolutePath, imageUrl)
    }

    private suspend fun downloadFile(path: String, url: String): String? {
        val download = DownloadUtils.download(url, path)
        return if (download) path else null
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

}
