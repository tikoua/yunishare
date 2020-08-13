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
        val videoUrl = "https://cdn.uneed.com/download/box_string200810.jpg"
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
            testShareWechatFriendText(text)
        }
        btWechatFriendImage.setOnClickListener {
            testShareWechatFriendImage(imageUrl)
        }
        btWechatFriendVideo.setOnClickListener {
            testShareWechatFriendVideo()
        }

        btWechatMomentText.setOnClickListener {
            testShareWechatMomentText(text)
        }
        btWechatMomentImage.setOnClickListener {
            testShareWechatMomentImage(imageUrl)
        }
        btWechatMomentVideo.setOnClickListener {
            testShareWechatMomentVideo()
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

    private fun testShareWechatFriendText(text: String) {
        YuniShare.share(
            this,
            ShareChannel.WechatFriend,
            InnerShareParams.buildWechatText().text(text).build()
        )
    }

    private fun testShareWechatFriendImage(imageUrl: String) {
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
                    ShareChannel.WechatFriend,
                    InnerShareParams.buildWechatImage().title("分享图片").imagePath(imagePath).build()
                )
            } catch (error: Throwable) {
                error.printStackTrace()
            }
        }
    }

    private fun testShareWechatMomentText(text: String) {
        YuniShare.share(
            this,
            ShareChannel.WechatMoment,
            InnerShareParams.buildWechatText().text(text).build()
        )
    }

    private fun testShareWechatMomentImage(imageUrl: String) {
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
                )
            } catch (error: Throwable) {
                error.printStackTrace()
            }
        }
    }

    private fun testShareWechatMomentVideo() {

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


    private fun testShareWechatFriendVideo() {
        YuniShare.share(
            this,
            ShareChannel.WechatFriend,
            InnerShareParams.buildWechatText().text("分享文本").build()
        )
    }

}
